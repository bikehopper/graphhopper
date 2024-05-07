package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.Cycleway;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OSMCyclewayParserTest {
    @Test
    public void testTags() {
        EnumEncodedValue<Cycleway> cyclewayAccessEnc =
            new EnumEncodedValue<>(Cycleway.KEY, Cycleway.class);
        cyclewayAccessEnc.init(new EncodedValue.InitializerConfig());
        OSMCyclewayParser parser = new OSMCyclewayParser(cyclewayAccessEnc);

        ReaderWay marketSt = new ReaderWay(1);
        marketSt.setTag("cycleway:right", "track");

        IntsRef edgeFlags = new IntsRef(1);
        IntsRef relationFlags = new IntsRef(1);

        parser.handleWayTags(edgeFlags, marketSt, relationFlags);
        assertEquals(Cycleway.TRACK, cyclewayAccessEnc.getEnum(false, edgeFlags));
    }
}
