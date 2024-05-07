package com.graphhopper.routing.util;


import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CayugaAlemanyTest {
    @Test
    public void test_cayuga() {
        ReaderWay cayuga = new ReaderWay(3);
        cayuga.setTag("highway", "residential");
        cayuga.setTag("maxspeed", "25 mph");
        cayuga.setTag("motor_vehicle", "destination");

        BikeFlagEncoder bike = new BikeFlagEncoder("bike");
        EncodingManager encodingManager = new EncodingManager.Builder().add(bike).build();
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        bike.handleWayTags(edgeFlags, cayuga);
        DecimalEncodedValue penaltyEnc = bike.getPenaltyEnc();

        // "motor_vehicle=no" (no cars allowed on the given way) should clamp
        // the way's penalty to the best assignable value.
        assertEquals(PenaltyCode.BEST.getValue(),
                penaltyEnc.getDecimal(false, edgeFlags));
    }

    @Test
    public void test_alemany() {
        ReaderWay alemany = new ReaderWay(3);
        alemany.setTag("cycleway:right", "lane");
        alemany.setTag("highway", "primary");
        alemany.setTag("maxspeed", "35 mph");
        alemany.setTag("oneway", "yes");

        BikeFlagEncoder bike = new BikeFlagEncoder("bike");
        EncodingManager encodingManager = new EncodingManager.Builder().add(bike).build();
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        bike.handleWayTags(edgeFlags, alemany);
        DecimalEncodedValue penaltyEnc = bike.getPenaltyEnc();

        // "motor_vehicle=no" (no cars allowed on the given way) should clamp
        // the way's penalty to the best assignable value.
        assertEquals(PenaltyCode.BEST.getValue(),
                penaltyEnc.getDecimal(false, edgeFlags));
    }
}