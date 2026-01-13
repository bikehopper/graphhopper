package com.graphhopper.routing.weighting;

import static com.graphhopper.util.Helper.createPointList3D;
import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.Orientation;
import com.graphhopper.routing.util.Bike2WeightFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TagParserManager;
import com.graphhopper.routing.util.TurnCostsConfig;
import com.graphhopper.routing.util.parsers.OrientationCalculator;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.IntsRef;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultTurnCostProviderTest {
    EncodingManager encodingManager = EncodingManager.start().add(new Bike2WeightFlagEncoder()).build();
    TagParserManager tagParserManager = TagParserManager.create("bike2");
    private DecimalEncodedValue orientationEnc;
    private OrientationCalculator orientationCalc;

    @BeforeEach
    public void setup() {
        List<EncodedValue> list = new ArrayList<>();
        EncodedValue.InitializerConfig init = new EncodedValue.InitializerConfig();
        orientationEnc = Orientation.create();
        orientationEnc.init(init);
        orientationCalc = new OrientationCalculator(orientationEnc);
        orientationCalc.createEncodedValues(encodingManager, list);
    }

    @Test
    public void testLeftTurn() {
        ReaderWay eighteenth = new ReaderWay(0);
        eighteenth.setTag("point_list", createPointList3D(
                37.76083, -122.43613, 0,
                37.7609, -122.43503, 0));
        ReaderWay castro = new ReaderWay(1);
        castro.setTag("point_list", createPointList3D(
                37.7609, -122.43503, 0,
                37.7625, -122.43519, 0));

        int ORIENTATION_BITS = 9;
        IntsRef edgeFlags1 = new IntsRef(ORIENTATION_BITS);
        IntsRef edgeFlags2 = new IntsRef(ORIENTATION_BITS);

        orientationCalc.handleWayTags(edgeFlags1, eighteenth, null);
        orientationCalc.handleWayTags(edgeFlags2, castro, null);

        BaseGraph graph = new BaseGraph.Builder(encodingManager).create();
        TurnCostProvider turnCostProvider = new DefaultTurnCostProvider(
                graph, new TurnCostsConfig(), orientationEnc);
//        assertEquals(45, turnCostProvider.calcTurnWeight(0, 0, 1));
    }
}
