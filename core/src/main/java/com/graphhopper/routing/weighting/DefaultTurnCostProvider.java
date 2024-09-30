/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.routing.weighting;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TurnCostsConfig;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

public class DefaultTurnCostProvider implements TurnCostProvider {
    private final double uTurnCosts;

    private final double minAngle;
    private final double minSharpAngle;
    private final double minUTurnAngle;

    private final double leftCosts;
    private final double leftSharpCosts;
    private final double rightCosts;
    private final double rightSharpCosts;
    private final BaseGraph graph;
    private final DecimalEncodedValue orientationEnc;

    // Graphhopper constructors that functionally do nothing.
    public DefaultTurnCostProvider(FlagEncoder encoder, TurnCostStorage turnCostStorage) {
        this(encoder, turnCostStorage, Weighting.INFINITE_U_TURN_COSTS);
    }

    public DefaultTurnCostProvider(FlagEncoder encoder, TurnCostStorage turnCostStorage, int uTurnCosts) {
        this.uTurnCosts = uTurnCosts < 0 ? Double.POSITIVE_INFINITY : uTurnCosts;
        TurnCostsConfig config = new TurnCostsConfig();
        this.minAngle = config.getMinAngleDegrees();
        this.minSharpAngle = config.getMinSharpAngleDegrees();
        this.minUTurnAngle = config.getMinUTurnAngleDegrees();

        this.leftCosts = config.getLeftCostSeconds();
        this.leftSharpCosts = config.getLeftSharpCostSeconds();
        this.rightCosts = config.getRightCostSeconds();
        this.rightSharpCosts = config.getRightSharpCostSeconds();
        this.graph = null;
        this.orientationEnc = null;
    }

    public DefaultTurnCostProvider(DecimalEncodedValue orientationEnc,
            Graph graph, TurnCostsConfig tcConfig) {
        this.uTurnCosts = tcConfig.getUTurnCostSeconds() < 0 ? Double.POSITIVE_INFINITY : tcConfig.getUTurnCostSeconds();
        if (graph.getTurnCostStorage() == null) {
            throw new IllegalArgumentException("No storage set to calculate turn weight");
        }

        this.orientationEnc = orientationEnc;

        this.minAngle = tcConfig.getMinAngleDegrees();
        this.minSharpAngle = tcConfig.getMinSharpAngleDegrees();
        this.minUTurnAngle = tcConfig.getMinUTurnAngleDegrees();

        this.leftCosts = tcConfig.getLeftCostSeconds();
        this.leftSharpCosts = tcConfig.getLeftSharpCostSeconds();
        this.rightCosts = tcConfig.getRightCostSeconds();
        this.rightSharpCosts = tcConfig.getRightSharpCostSeconds();

        this.graph = graph.getBaseGraph();
    }

    @Override
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        if (!EdgeIterator.Edge.isValid(inEdge) || !EdgeIterator.Edge.isValid(outEdge))
            return 0;

        // note that the u-turn costs overwrite any turn costs set in TurnCostStorage
        if (inEdge == outEdge) return uTurnCosts;

        if (orientationEnc != null) {
            double changeAngle = calcChangeAngle(inEdge, viaNode, outEdge);
            if (changeAngle > -minAngle && changeAngle < minAngle)
                return 0.0;
            else if (changeAngle >= minAngle && changeAngle < minSharpAngle)
                return rightCosts;
            else if (changeAngle >= minSharpAngle && changeAngle <= minUTurnAngle)
                return rightSharpCosts;
            else if (changeAngle <= -minAngle && changeAngle > -minSharpAngle)
                return leftCosts;
            else if (changeAngle <= -minSharpAngle && changeAngle >= -minUTurnAngle)
                return leftSharpCosts;

            // Too sharp turn is like an u-turn.
            return uTurnCosts;
        }
        return 0;
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return (long) (1000 * calcTurnWeight(inEdge, viaNode, outEdge));
    }

    @Override
    public String toString() { return "default_tcp_"; }

    double calcChangeAngle(int inEdge, int viaNode, int outEdge) {
        EdgeIteratorState inEdgeState = graph.getEdgeIteratorStateForKey(inEdge);
        EdgeIteratorState outEdgeState = graph.getEdgeIteratorStateForKey(outEdge);
        boolean inEdgeReverse = !graph.isAdjacentToNode(inEdge, viaNode);
        double prevAzimuth = orientationEnc.getDecimal(inEdgeReverse, inEdgeState.getFlags());

        boolean outEdgeReverse = !graph.isAdjacentToNode(outEdge, viaNode);
        double azimuth = orientationEnc.getDecimal(outEdgeReverse, outEdgeState.getFlags());

        // bring parallel to prevOrientation
        if (azimuth >= 180) azimuth -= 180;
        else azimuth += 180;

        double changeAngle = azimuth - prevAzimuth;

        // keep in [-180, 180]
        if (changeAngle > 180) changeAngle -= 360;
        else if (changeAngle < -180) changeAngle += 360;
        return changeAngle;
    }
}
