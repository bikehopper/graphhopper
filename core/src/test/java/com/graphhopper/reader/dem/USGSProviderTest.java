package com.graphhopper.reader.dem;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class USGSProviderTest {

    USGSProvider provider = new USGSProvider("");

    @Test
    public void testFilename() {
        assertEquals("ned19_n38x00w122x50",
                provider.getFileName(38, -122.5));
        assertEquals("ned19_n37x75w122x00",
                provider.getFileName(37, -122));
    }

    @Test
    public void testMinLat() {
        assertEquals(37.75, provider.getMinLatForTile(37.99));
        assertEquals(38, provider.getMinLatForTile(38));
    }
}
