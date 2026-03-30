package com.graphhopper.routing.util.parsers;

import static com.graphhopper.util.Helper.createPointList3D;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.Orientation;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class OrientationCalculatorTest {
    private EncodingManager encodingManager = EncodingManager.create();

    @Test
    public void testCalculate() {
        List<EncodedValue> list = new ArrayList<>();

        ReaderWay way = new ReaderWay(0);
        way.setTag("point_list", createPointList3D(
                37.7609, -122.43503, 0,
                37.76083, -122.43606, 0));

        IntsRef edgeFlags = new IntsRef(9);

        EncodedValue.InitializerConfig init = new EncodedValue.InitializerConfig();
        DecimalEncodedValue orientationEnc = Orientation.create();
        orientationEnc.init(init);
        OrientationCalculator calc = new OrientationCalculator(orientationEnc);
        calc.createEncodedValues(encodingManager, list);
        calc.handleWayTags(edgeFlags, way, null);
        System.out.println("Examine");
    }
}
