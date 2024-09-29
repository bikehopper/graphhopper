package com.graphhopper.routing.ev;

public class Orientation {
    public static final String KEY = "orientation";

    public static DecimalEncodedValue create() {
        return new DecimalEncodedValueImpl(KEY, 8, 0, 360 / 60.0,
                false, false, true, false);
    }
}
