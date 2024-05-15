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
    SLIGHT_PREFER(3.0),
    UNCHANGED(5.0),
    SLIGHT_AVOID(7.5),
    AVOID(8.0),
    AVOID_MORE(8.5),
    BAD(9.0),
    VERY_BAD(10.0),
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

    public PenaltyCode tickUpBy(int n) {
        PenaltyCode[] codes = PenaltyCode.values();
        int current = Arrays.asList(codes).indexOf(this);
        return codes[Math.min(current + n, codes.length - 1)];
    }

    public PenaltyCode tickDownBy(int n) {
        PenaltyCode[] codes = PenaltyCode.values();
        int current = Arrays.asList(codes).indexOf(this);
        return codes[Math.max(current - n, 0)];
    }

    public static PenaltyCode from(double value) {
        PenaltyCode[] codes = PenaltyCode.values();
        for (PenaltyCode c : codes) {
            if (c.getValue() >= value)
                return c;
        }
        return codes[codes.length - 1];
    }
}
