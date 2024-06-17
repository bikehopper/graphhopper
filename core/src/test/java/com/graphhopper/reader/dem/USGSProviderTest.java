package com.graphhopper.reader.dem;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class USGSProviderTest {
    @Test
    public void testFilename() {
        USGSProvider provider = new USGSProvider("");
        assertEquals("ned19_n38x00w122x50",
                provider.getFileName(38.00, -122.50));
    }

    @Test
    public void testMinLat() {
        USGSProvider provider = new USGSProvider("");
        assertEquals(37.75, provider.getMinLatForTile(37.99));
        assertEquals(38, provider.getMinLatForTile(38));
    }
}
