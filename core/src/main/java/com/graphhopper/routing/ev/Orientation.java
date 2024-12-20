package com.graphhopper.routing.ev;

public class Orientation {
    public static final String KEY = "orientation";

    public static DecimalEncodedValue create() {
        return new DecimalEncodedValueImpl(KEY, 5, 0, 360 / 30.0, false, false,
                true, false);
    }
}

//(String name, int bits, double minValue, double factor, boolean defaultIsInfinity,
// boolean negateReverseDirection, boolean storeTwoDirections, boolean useMaximumAsInfinity)