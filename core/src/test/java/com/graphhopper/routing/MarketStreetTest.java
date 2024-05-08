package com.graphhopper.routing;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PenaltyCode;
import com.graphhopper.routing.util.TagParserManager;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarketStreetTest {
    protected BikeFlagEncoder encoder;
    protected TagParserManager encodingManager;

    @BeforeEach
    void setUp() {
        encodingManager = TagParserManager.create(encoder = new BikeFlagEncoder("bike"));
    }

    @Test
    public void testCarFree() {
        ReaderWay marketSt = new ReaderWay(0);
        marketSt.setTag("cycleway:right", "shared_lane");
        marketSt.setTag("highway", "tertiary");
        marketSt.setTag("oneway", "yes");
        marketSt.setTag("motor_vehicle", "no");

        BikeFlagEncoder bike = new BikeFlagEncoder("bike");
        EncodingManager encodingManager = new EncodingManager.Builder().add(bike).build();
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        bike.handleWayTags(edgeFlags, marketSt);
        DecimalEncodedValue penaltyEnc = bike.getPenaltyEnc();

        // "motor_vehicle=no" (no cars allowed on the given way) should clamp
        // the way's penalty to the best assignable value.
        assertEquals(PenaltyCode.BEST.getValue(),
                penaltyEnc.getDecimal(false, edgeFlags));
    }

    @Test
    public void testPartiallyCarFree() {
        ReaderWay marketSt = new ReaderWay(0);
        marketSt.setTag("cycleway:right", "shared_lane");
        marketSt.setTag("highway", "tertiary");
        marketSt.setTag("oneway", "yes");
        marketSt.setTag("motor_vehicle", "destination");

        BikeFlagEncoder bike = new BikeFlagEncoder("bike");
        EncodingManager encodingManager = new EncodingManager.Builder().add(bike).build();
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        bike.handleWayTags(edgeFlags, marketSt);
        DecimalEncodedValue penaltyEnc = bike.getPenaltyEnc();

        // "motor_vehicle=no" (no cars allowed on the given way) should clamp
        // the way's penalty to the best assignable value.
        assertEquals(PenaltyCode.BEST.getValue(),
                penaltyEnc.getDecimal(false, edgeFlags));
    }

    // https://www.openstreetmap.org/way/224200967
    @Test
    public void test_way224200967() {
        ReaderWay marketSt = new ReaderWay(3);
        marketSt.setTag("cycleway:both", "shared_lane");
        marketSt.setTag("cycleway:right:lane", "pictogram");
        marketSt.setTag("foot", "yes");
        marketSt.setTag("highway", "tertiary");
        marketSt.setTag("maxspeed", "20mph");
        marketSt.setTag("motor_vehicle", "no");
        marketSt.setTag("sidewalk", "separate");

        BikeFlagEncoder bike = new BikeFlagEncoder("bike");
        EncodingManager encodingManager = new EncodingManager.Builder().add(bike).build();
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        bike.handleWayTags(edgeFlags, marketSt);
        DecimalEncodedValue penaltyEnc = bike.getPenaltyEnc();

        // "motor_vehicle=no" (no cars allowed on the given way) should clamp
        // the way's penalty to the best assignable value.
        assertEquals(PenaltyCode.BEST.getValue(),
                penaltyEnc.getDecimal(false, edgeFlags));
    }


    @Test
    public void test_way204964839() {
        ReaderWay marketSt = new ReaderWay(3);
        marketSt.setTag("cycleway:right", "shared_lane");
        marketSt.setTag("highway", "primary");
        marketSt.setTag("maxspeed", "20mph");

        IntsRef relFlags = encodingManager.handleRelationTags(new ReaderRelation(0),
            encodingManager.createRelationFlags());
        IntsRef edgeFlags = encodingManager.handleWayTags(marketSt, relFlags);
        DecimalEncodedValue penaltyEnc = encoder.getPenaltyEnc();

        // Penalty should be high because "shared_lane" is
        // unsafe cycling infrastructure mixed in with car traffic.
        assertEquals(PenaltyCode.BAD.getValue(),
                penaltyEnc.getDecimal(false, edgeFlags));
    }
}
