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
package com.graphhopper.routing.util;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import static com.graphhopper.routing.util.PenaltyCode.*;

class GradeBoundary implements Comparable<GradeBoundary> {
    final Integer lowerBoundary;
    final Integer upperBoundary;

    GradeBoundary(Integer lowerBoundary, Integer upperBoundary) {
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
    }

    @Override
    public int compareTo(GradeBoundary o) {
        // Assume GradeBoundaries are inclusive boundaries, and do not overlap
        if (this.upperBoundary < o.lowerBoundary)
            return -1;
        if (this.lowerBoundary > o.upperBoundary)
            return 1;
        return 0;
    }
}

/**
 * Stores two speed values into an edge to support avoiding too much incline
 *
 * @author Peter Karich
 */
public class Bike2WeightFlagEncoder extends BikeFlagEncoder {
    // This map takes a GradeBoundary and a Penalty value, and returns a new Penalty
    Map<GradeBoundary, Function<Double, Double>> gradePenaltyMap = new HashMap<>();
    List<GradeBoundary> grades = new ArrayList<>(); // sorted

    GradeBoundary boundaryFor(int grade) {
        return grades.get(Collections.binarySearch(grades, new GradeBoundary(grade, grade)));
    }

    public Bike2WeightFlagEncoder() {
        this(new PMap());
    }

    public Bike2WeightFlagEncoder(PMap properties) {
        super(new PMap(properties).putObject("speed_two_directions", true).putObject("name",
                properties.getString("name", "bike2")));

        // Define grade boundaries. Order for `grades.add` matters.
        GradeBoundary extremeDownGrade = new GradeBoundary(-100, -16);
        grades.add(extremeDownGrade);
        GradeBoundary strongDownGrade = new GradeBoundary(-15, -12);
        grades.add(strongDownGrade);
        GradeBoundary mediumDownGrade = new GradeBoundary(-11, -8);
        grades.add(mediumDownGrade);
        GradeBoundary mildDownGrade = new GradeBoundary(-7, -4);
        grades.add(mildDownGrade);
        GradeBoundary neutralGrade = new GradeBoundary(-3, 3);
        grades.add(neutralGrade);
        GradeBoundary mildUpGrade = new GradeBoundary(4, 7);
        grades.add(mildUpGrade);
        GradeBoundary mediumUpGrade = new GradeBoundary(8, 11);
        grades.add(mediumUpGrade);
        GradeBoundary strongUpGrade = new GradeBoundary(12, 15);
        grades.add(strongUpGrade);
        GradeBoundary extremeUpGrade = new GradeBoundary(16, 100);
        grades.add(extremeUpGrade);

        // At downwards grades, the penalty is lessened
        gradePenaltyMap.put(strongDownGrade, (p) -> {
            // TODO
            return p;
        });
        gradePenaltyMap.put(mediumDownGrade, (p) -> {
            // TODO
            return p;
        });
        gradePenaltyMap.put(mildDownGrade, (p) -> {
            // TODO
            return p;
        });

        // At a neutral grade, the penalty is unchanged
        gradePenaltyMap.put(neutralGrade, (p) -> p);

        // At upwards grades, the penalty is increased
        gradePenaltyMap.put(mildUpGrade, (p) -> {
            // TODO
            return p;
        });
        gradePenaltyMap.put(mediumUpGrade, (p) -> {
            // TODO
            return p;
        });
        gradePenaltyMap.put(strongUpGrade, (p) -> {
            // TODO
            return p;
        });

        // At extreme grades, the penalty is vastly increased
        gradePenaltyMap.put(extremeDownGrade, (p) -> {
            return REACH_DESTINATION.getValue();
        });
        gradePenaltyMap.put(extremeUpGrade, (p) -> {
            return REACH_DESTINATION.getValue();
        });
    }

    @Override
    public void applyWayTags(ReaderWay way, EdgeIteratorState edge) {
        int grade = edge.getGrade();
        IntsRef edgeFlags = edge.getFlags();
        Double forwardPenalty = penaltyEnc.getDecimal(false, edgeFlags);
        Double newForwardPenalty = gradePenaltyMap.get(boundaryFor(grade)).apply(forwardPenalty);
        penaltyEnc.setDecimal(false, edgeFlags, newForwardPenalty);

        Double backwardPenalty = penaltyEnc.getDecimal(true, edgeFlags);
        Double newBackwardPenalty = gradePenaltyMap.get(boundaryFor(-1 * grade)).apply(backwardPenalty);
        penaltyEnc.setDecimal(true, edgeFlags, newBackwardPenalty);

        super.applyWayTags(way, edge);
    }
}
