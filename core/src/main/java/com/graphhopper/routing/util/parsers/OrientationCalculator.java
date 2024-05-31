package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.Orientation;
import com.graphhopper.storage.IntsRef;
import java.util.List;

public class OrientationCalculator implements TagParser {

    private final DecimalEncodedValue orientationEnc;

    public OrientationCalculator() {
        this.orientationEnc = Orientation.create();
    }

    @Override
    public void createEncodedValues(EncodedValueLookup lookup,
            List<EncodedValue> registerNewEncodedValue) {
        registerNewEncodedValue.add(orientationEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way,
            IntsRef relationFlags) {

        // 1. Get coordinates
        // 2. Use AngleCalc.getOrientation()
        // 3. Set OrientationEnc to the result of (2)
        return null;
    }
}
