package com.graphhopper.routing.ev;

public class Orientation {
    public static final String KEY = "orientation";

    public static DecimalEncodedValue create() {
        return new DecimalEncodedValueImpl(KEY, 5, -Math.PI, 2 * Math.PI / 30,
                false, false, true, false);
    }
}
