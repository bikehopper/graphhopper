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

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Used to store a penalty value in the way flags of an edge. Used in
 * combination with PenaltyWeighting
 *
 * @author Hazel Court
 */
public enum PenaltyCode {
    // Declare in ascending order
    BEST(1.0),
    VERY_NICE(1.5),
    PREFER(2.0),
    SLIGHT_PREFER(2.5),
    UNCHANGED(5.0),
    SLIGHT_AVOID(7.5),
    AVOID(8.0),
    AVOID_MORE(8.5),
    BAD(9.0),
    VERY_BAD(9.5),
    REACH_DESTINATION(12),
    EXCLUDE(15);

    private final double value;

    PenaltyCode(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public static double getFactor(double value) {
        return (double) value;
    }

    public static double getValue(double value) {
        return getFactor(value);
    }

    public PenaltyCode tickUp() {
        switch (this) {
            case BEST:
                return PREFER;
            case PREFER:
                return SLIGHT_PREFER;
            case SLIGHT_PREFER:
                return UNCHANGED;
            case UNCHANGED:
                return SLIGHT_AVOID;
            case SLIGHT_AVOID:
                return AVOID;
            case AVOID:
                return AVOID_MORE;
            case AVOID_MORE:
                return BAD;
            case BAD:
                return VERY_BAD;
            case VERY_BAD:
                return REACH_DESTINATION;
            default:
                return EXCLUDE;
        }
    }

    public PenaltyCode tickDown() {
        switch (this) {
            case EXCLUDE:
                return REACH_DESTINATION;
            case REACH_DESTINATION:
                return VERY_BAD;
            case VERY_BAD:
                return BAD;
            case BAD:
                return AVOID_MORE;
            case AVOID_MORE:
                return AVOID;
            case AVOID:
                return SLIGHT_AVOID;
            case SLIGHT_AVOID:
                return UNCHANGED;
            case UNCHANGED:
                return SLIGHT_PREFER;
            case SLIGHT_PREFER:
                return PREFER;
            case PREFER:
                return VERY_NICE;
            default:
                return BEST;
        }
    }

    public static PenaltyCode from(double value) {
        int i = Collections.binarySearch(
                Arrays.stream(PenaltyCode.values())
                        .map(PenaltyCode::getValue)
                        .collect(Collectors.toList()),
                value);
        return PenaltyCode.values()[i];
    }
}
