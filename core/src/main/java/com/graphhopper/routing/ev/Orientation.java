package com.graphhopper.routing.ev;

public class Orientation {
    public static final String KEY = "orientation";

    public static DecimalEncodedValue create() {
        return new DecimalEncodedValueImpl(KEY, 9, -Math.PI, 2 * Math.PI / 360,
                false, false, true, false);
    }
}
