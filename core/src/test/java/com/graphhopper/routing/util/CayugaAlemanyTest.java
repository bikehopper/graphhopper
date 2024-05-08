package com.graphhopper.routing.util;


import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CayugaAlemanyTest {
    protected BikeFlagEncoder encoder;
    protected TagParserManager encodingManager;

    @BeforeEach
    public void setUp() {
        encodingManager = TagParserManager.create(encoder = new BikeFlagEncoder("bike"));
    }

    @Test
    public void test_cayuga() {
        ReaderWay cayuga = new ReaderWay(3);
        cayuga.setTag("highway", "residential");
        cayuga.setTag("maxspeed", "25 mph");
        cayuga.setTag("motor_vehicle", "destination");

        IntsRef relFlags = encodingManager.handleRelationTags(new ReaderRelation(0),
                encodingManager.createRelationFlags());
        IntsRef edgeFlags = encodingManager.handleWayTags(cayuga, relFlags);
        DecimalEncodedValue penaltyEnc = encoder.getPenaltyEnc();

        // Penalty should be SLIGHT_PREFER because of highway=residential.
        assertEquals(PenaltyCode.SLIGHT_PREFER.getValue(),
                penaltyEnc.getDecimal(false, edgeFlags));
    }

    @Test
    public void test_alemany() {
        ReaderWay alemany = new ReaderWay(3);
        alemany.setTag("cycleway:right", "lane");
        alemany.setTag("highway", "primary");
        alemany.setTag("maxspeed", "35 mph");
        alemany.setTag("oneway", "yes");

        IntsRef relFlags = encodingManager.handleRelationTags(new ReaderRelation(0),
                encodingManager.createRelationFlags());
        IntsRef edgeFlags = encodingManager.handleWayTags(alemany, relFlags);
        DecimalEncodedValue penaltyEnc = encoder.getPenaltyEnc();

        // Penalty should be PREFER because of cycleway:right=lane.
        assertEquals(PenaltyCode.PREFER.getValue(),
                penaltyEnc.getDecimal(false, edgeFlags));
    }
}
