package com.graphhopper.routing.util.parsers;

import static com.graphhopper.util.AngleCalc.ANGLE_CALC;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PointList;
import java.util.List;

public class OrientationCalculator implements TagParser {
    private final DecimalEncodedValue orientationEnc;

    public OrientationCalculator(DecimalEncodedValue orientationEnc) {
        this.orientationEnc = orientationEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup lookup,
            List<EncodedValue> registerNewEncodedValue) {
        registerNewEncodedValue.add(orientationEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way,
            IntsRef relationFlags) {
        PointList points = way.getTag("point_list", null);
        if (points != null) {
            double azimuth = ANGLE_CALC.calcAzimuth(
                    points.getLat(points.size() - 2), points.getLon(points.size() - 2),
                    points.getLat(points.size() - 1), points.getLon(points.size() - 1));
            orientationEnc.setDecimal(false, edgeFlags, azimuth);

            double revAzimuth = ANGLE_CALC.calcAzimuth(
                    points.getLat(1), points.getLon(1),
                    points.getLat(0), points.getLon(0)
            );
            orientationEnc.setDecimal(true, edgeFlags, revAzimuth);
        }
        return null;
    }
}
