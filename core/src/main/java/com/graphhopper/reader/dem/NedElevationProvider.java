package com.graphhopper.reader.dem;

import static com.graphhopper.util.Helper.close;

import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.xmlgraphics.image.codec.tiff.TIFFDecodeParam;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;

public class NedElevationProvider extends AbstractTiffElevationProvider {

    public NedElevationProvider(String baseUrl, String cacheDir,
            String downloaderName, int width, int height, int latDegree,
            int lonDegree) {
        super(baseUrl, cacheDir, downloaderName, width, height, latDegree,
                lonDegree);
    }

    public static void main(String[] args) {
        NedElevationProvider elevationProvider = new NedElevationProvider(
                "", "/tmp/", "", 10812, 10812,
                1, 1);

        // Market Street ~-5ft to 260ft.
        System.out.println("Elevation: " + elevationProvider.getEle(37.7903317182555, -122.39999824547087) + "m");
        System.out.println("Elevation: " + elevationProvider.getEle(37.79112431722635, -122.39901032204128) + "m");

        // Mount Davidson: expected: 290m+ actual: 77.
        System.out.println("Elevation: " + elevationProvider.getEle(37.7383486,-122.4544909) + "m");

        // San Bruno Mountain -> actual: 111m
        System.out.println("Elevation: " + elevationProvider.getEle(37.687365646377906, -122.4354465347176) + "m");

        // Bay --> 0m
        System.out.println("Elevation: " + elevationProvider.getEle(37.72314991895665, -122.30819708256892) + "m");

    }

    @Override
    boolean isOutsideSupportedArea(double lat, double lon) {
        return lat < 37 || lat > 38 || lon < -123 || lon > -122;
    }

    @Override
    int getMinLatForTile(double lat) {
        return 37;
    }

    @Override
    int getMinLonForTile(double lon) {
        return -123;
    }

    @Override
    String getFileNameOfLocalFile(double lat, double lon) {
        return "n38w123.tif";
    }

    @Override
    String getFileName(double lat, double lon) {
        return "n38w123";
    }

    @Override
    String getDownloadURL(double lat, double lon) {
        return "";
    }

    @Override
    Raster generateRasterFromFile(File file, String tifName) {
        SeekableStream ss = null;
        try {
            InputStream is = new FileInputStream(file);
            ss = SeekableStream.wrapInputStream(is, true);
            TIFFImageDecoder imageDecoder = new TIFFImageDecoder(ss, new TIFFDecodeParam());
            return imageDecoder.decodeAsRaster();
        } catch (Exception e) {
            throw new RuntimeException("Can't decode " + file.getName(), e);
        } finally {
            if (ss != null)
                close(ss);
        }
    }
}
