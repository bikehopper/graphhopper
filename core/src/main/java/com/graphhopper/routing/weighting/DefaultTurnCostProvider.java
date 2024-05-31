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

import static com.graphhopper.util.AngleCalc.ANGLE_CALC;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Orientation;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TurnCostsConfig;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

public class DefaultTurnCostProvider implements TurnCostProvider {
    private double uTurnCosts = 60.0;
    private DecimalEncodedValue orientationEnc;

    private double minLeftRad, minRightRad, maxLeftRad, maxRightRad;
    private double leftCost, rightCost;
    private Graph graph;

    public DefaultTurnCostProvider(FlagEncoder encoder, TurnCostStorage turnCostStorage) {
        this(encoder, turnCostStorage, Weighting.INFINITE_U_TURN_COSTS);
    }

    public DefaultTurnCostProvider(FlagEncoder encoder, Graph graph, TurnCostsConfig turnCostsConfig) {
        this.graph = graph;
        String key = Orientation.KEY;
        this.orientationEnc = encoder.hasEncodedValue(key) ? encoder.getDecimalEncodedValue(key) : null;

        this.minLeftRad = Math.toRadians(turnCostsConfig.getMinLeftAngle());
        this.minRightRad = Math.toRadians(turnCostsConfig.getMinRightAngle());
        this.maxLeftRad = Math.toRadians(turnCostsConfig.getMaxLeftAngle());
        this.maxRightRad = Math.toRadians(turnCostsConfig.getMaxRightAngle());
        this.leftCost = turnCostsConfig.getLeftCost();
        this.rightCost = turnCostsConfig.getRightCost();
    }

    public DefaultTurnCostProvider(BooleanEncodedValue turnRestrictionsEnc,
            TurnCostStorage turnCostStorage, TurnCostsConfig turnCostsConfig) {
        this.uTurnCosts = turnCostsConfig.getUTurnCosts();
//        this.turnCostStorage = turnCostStorage;
    }

    public DefaultTurnCostProvider(FlagEncoder encoder, TurnCostStorage turnCostStorage, int infiniteUTurnCosts) {
    }

    @Override
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        if (!EdgeIterator.Edge.isValid(inEdge) || !EdgeIterator.Edge.isValid(
                outEdge))
            return 0;
        double tCost =  0;
        if (inEdge == outEdge) return uTurnCosts;

        if (orientationEnc != null) {
            double changeAngle = calcChangeAngle(inEdge, viaNode, outEdge);
            if (changeAngle > minRightRad && changeAngle < minLeftRad)
                tCost = 0;  // straight
            else if (changeAngle >= minLeftRad && changeAngle <= maxLeftRad)
                tCost += leftCost;
            else if (changeAngle <= minRightRad && changeAngle >= maxRightRad)
                tCost += rightCost;
            else
                return Double.POSITIVE_INFINITY; // too sharp turn
        }
        System.out.println("edge=" + inEdge + " to edge=" + outEdge + ", turncost=" + tCost);
        return tCost;
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return (long) (1000 * calcTurnWeight(inEdge, viaNode, outEdge));
    }

    @Override
    public String toString() {
        return "default_tcp_" + "left_cost=" + leftCost + ", right_cost="
                + rightCost + ", u_turn_cost=" + uTurnCosts;
    }

    double calcChangeAngle(int inEdge, int viaNode, int outEdge) {
        EdgeIteratorState inEdgeState = graph.getEdgeIteratorStateForKey(inEdge);
        EdgeIteratorState outEdgeState = graph.getEdgeIteratorStateForKey(outEdge);
        boolean inEdgeReverse = !graph.isAdjacentToNode(inEdge, viaNode);
        double prevOrientation = orientationEnc.getDecimal(inEdgeReverse, inEdgeState.getFlags());

        boolean outEdgeReverse = !graph.isAdjacentToNode(outEdge, viaNode);
        double orientation = orientationEnc.getDecimal(outEdgeReverse, outEdgeState.getFlags());

        // bring parallel to prevOrientation
        if (orientation >= 0)
            orientation -= Math.PI;
        else
            orientation += Math.PI;
        prevOrientation = ANGLE_CALC.alignOrientation(orientation, prevOrientation);
        double changeAngle = orientation - prevOrientation;
        if (changeAngle > Math.PI) changeAngle -= 2 * Math.PI;
        else if (changeAngle < -Math.PI) changeAngle += 2 * Math.PI;
        return changeAngle;
    }
}
