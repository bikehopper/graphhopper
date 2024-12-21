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
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;

public class DefaultTurnCostProvider implements TurnCostProvider {
    private final DecimalEncodedValue orientationEnc;
    private final BaseGraph graph;

    public DefaultTurnCostProvider(DecimalEncodedValue orientationEnc, BaseGraph graph) {
        this.orientationEnc = orientationEnc;
        this.graph = graph;
    }

    @Override
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        return 0;
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return 0;
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
