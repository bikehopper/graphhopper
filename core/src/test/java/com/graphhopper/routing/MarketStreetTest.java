package com.graphhopper.routing;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PenaltyCode;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarketStreetTest {

    @Test
    public void testMarketStreet() {

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
}
