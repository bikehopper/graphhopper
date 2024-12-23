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
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

public class DefaultTurnCostProvider implements TurnCostProvider {
    private final DecimalEncodedValue orientationEnc;
    private final BaseGraph graph;
    private final TurnCostsConfig config;

    private final double leftCosts, leftSharpCosts;
    private final double rightCosts, rightSharpCosts;
    private final double uTurnCosts;

    private final double minAngle, minSharpAngle;
    private final double minUTurnAngle;

    public DefaultTurnCostProvider(DecimalEncodedValue orientationEnc, BaseGraph graph, TurnCostsConfig config) {
        this.orientationEnc = orientationEnc;
        this.graph = graph;
        this.config = config;

        this.leftCosts = this.config.getLeftCostsSeconds();
        this.leftSharpCosts = this.config.getLeftSharpCostsSeconds();
        this.rightCosts = this.config.getRightCostsSeconds();
        this.rightSharpCosts = this.config.getRightSharpCostsSeconds();
        this.uTurnCosts = this.config.getUTurnCostsSeconds();

        this.minAngle = this.config.getMinAngleDegrees();
        this.minSharpAngle = this.config.getMinSharpAngleDegrees();
        this.minUTurnAngle = this.config.getMinUTurnAngleDegrees();
    }

    // Ensures that unit tests compile.
    public DefaultTurnCostProvider(FlagEncoder encoder, TurnCostStorage turnCostStorage) {
        this(encoder, turnCostStorage, 0);
    }

    public DefaultTurnCostProvider(FlagEncoder encoder, TurnCostStorage turnCostStorage,
            int uTurnCosts) {
        this.orientationEnc = null;
        this.graph = null;
        this.config = new TurnCostsConfig();

        this.leftCosts = this.config.getLeftCostsSeconds();
        this.leftSharpCosts = this.config.getLeftSharpCostsSeconds();
        this.rightCosts = this.config.getRightCostsSeconds();
        this.rightSharpCosts = this.config.getRightSharpCostsSeconds();
        this.uTurnCosts = this.config.getUTurnCostsSeconds();

        this.minAngle = this.config.getMinAngleDegrees();
        this.minSharpAngle = this.config.getMinSharpAngleDegrees();
        this.minUTurnAngle = this.config.getMinUTurnAngleDegrees();
    }

    @Override
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        if (!EdgeIterator.Edge.isValid(inEdge) || !EdgeIterator.Edge.isValid(outEdge))
            return 0;
        if (inEdge == outEdge)
            return config.getUTurnCostsSeconds();

//        System.out.println("calcTurnWeight: " + inEdge + "->" + viaNode + "->" + outEdge);
        // also need to handle restricted turns using TCS, but maybe later.

        if (orientationEnc != null) {
           double angle = calcChangeAngle(inEdge, viaNode, outEdge);
            if (angle >= minAngle && angle < minSharpAngle)
                return rightCosts;
            else if (angle >= minSharpAngle && angle <= minUTurnAngle)
                return rightSharpCosts;
            else if (angle <= -minAngle && angle > -minSharpAngle)
                return leftCosts;
            else if (angle >= -minSharpAngle && angle < -minUTurnAngle)
                return leftSharpCosts;

            // Anything else that's too sharp is practically a u-turn.
            return uTurnCosts;
        }
        return 0;
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return (long)(1000 * calcTurnWeight(inEdge, viaNode, outEdge));
    }

    @Override
    public String toString() {
        return "default_tcp";
    }

    double calcChangeAngle(int inEdge, int viaNode, int outEdge) {
        EdgeIteratorState edge1 = graph.getEdgeIteratorState(inEdge, viaNode);
        EdgeIteratorState edge2 = graph.getEdgeIteratorState(outEdge, viaNode);
        boolean inEdgeReverse = !graph.isAdjacentToNode(inEdge, viaNode);
        boolean outEdgeReverse = !graph.isAdjacentToNode(outEdge, viaNode);

        double prevAzimuth = orientationEnc.getDecimal(inEdgeReverse,
                edge1.getFlags());
        double azimuth = orientationEnc.getDecimal(outEdgeReverse,
                edge2.getFlags());

        azimuth += (azimuth >= 180 ? -180 : 180);
        double changeAngle = azimuth - prevAzimuth;
        if (changeAngle > 180) changeAngle -= 360;
        else if (changeAngle < -180) changeAngle += 360;
        return changeAngle;
    }
}
