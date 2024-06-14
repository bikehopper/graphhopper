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

    String filename = "n38w123";

    public NedElevationProvider(String cacheDir) {
        this("", cacheDir, "", 10812, 10812, 1, 1);
    }

    public NedElevationProvider(String baseUrl, String cacheDir,
            String downloaderName, int width, int height, int latDegree,
            int lonDegree) {
        super(baseUrl, cacheDir, downloaderName, width, height, latDegree,
                lonDegree);
        setInterpolate(true);
    }

    public static void main(String[] args) {
        NedElevationProvider elevationProvider = new NedElevationProvider("/tmp/");

        // Market Street ~-5ft to 260ft in prod.
        System.out.println("Elevation: " + elevationProvider.getEle(37.7903317182555, -122.39999824547087) + "m");
        System.out.println("Elevation: " + elevationProvider.getEle(37.79112431722635, -122.39901032204128) + "m");

        // Mount Davidson, expected: ~283m
        System.out.println("Elevation: " + elevationProvider.getEle(37.738259, -122.45463) + "m");
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
        return filename + ".tif";
    }

    @Override
    String getFileName(double lat, double lon) {
        return filename;
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
