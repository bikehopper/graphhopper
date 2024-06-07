package com.graphhopper.routing.util.parsers;

import static com.graphhopper.util.AngleCalc.ANGLE_CALC;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.Orientation;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import java.util.List;

public class OrientationCalculator implements TagParser {

    private final DecimalEncodedValue orientationEnc;

    public OrientationCalculator() {
        this.orientationEnc = Orientation.create();
    }

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
        if (points == null) {
            System.out.println("WARN: no points found for way=" + way.getId());
        } else {
            // Use the end of the edge to calculate a way's orientation.
            GHPoint3D point1 = points.get(points.size() - 2);
            GHPoint3D point2 = points.get(points.size() - 1);
            double orientationRad = ANGLE_CALC.calcOrientation(
                    point1.getLat(), point1.getLon(),
                    point2.getLat(), point2.getLon());
            orientationEnc.setDecimal(false, edgeFlags, orientationRad);

            // Use the beginning of the edge to calculate a way's orientation
            // for the reverse direction.
            GHPoint3D point1Rev = points.get(1);
            GHPoint3D point2Rev = points.get(0);
            double orientationRevRad = ANGLE_CALC.calcOrientation(
                    point1Rev.getLat(), point1Rev.getLon(),
                    point2Rev.getLat(), point2Rev.getLon());
            orientationEnc.setDecimal(true, edgeFlags, orientationRevRad);
        }
        return edgeFlags;
    }
}
