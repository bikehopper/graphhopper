package com.graphhopper.reader.dem;

import static com.graphhopper.util.Helper.close;

import com.graphhopper.storage.DataAccess;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.xmlgraphics.image.codec.tiff.TIFFDecodeParam;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;

public class USGSProvider extends AbstractTiffElevationProvider {

    String filename = "ned19_n37x75_w122x00_gscale";

    public USGSProvider(String cacheDir) {
        this("", cacheDir, "", 8112, 8112, 0.25, 0.25);
    }

    public USGSProvider(String baseUrl, String cacheDir,
            String downloaderName, int width, int height, double latDegree,
            double lonDegree) {
        super(baseUrl, cacheDir, downloaderName, width, height, latDegree,
                lonDegree);
        setInterpolate(true);
    }

    public static void main(String[] args) {
        USGSProvider elevationProvider = new USGSProvider("/tmp/");

        // Market Street ~-5ft to 260ft in prod.
//        System.out.println("Elevation: " + elevationProvider.getEle(37.7903317182555, -122.39999824547087) + "m");
//        System.out.println("Elevation: " + elevationProvider.getEle(37.79112431722635, -122.39901032204128) + "m");
        System.out.println("Elevation: " + elevationProvider.getEle(37.7499, -122.0001) + "m");
        System.out.println("Elevation: " + elevationProvider.getEle(37.5001, -122.0001) + "m");
    }

    @Override
    boolean isOutsideSupportedArea(double lat, double lon) {
        return lat < 37 || lat > 38 || lon < -123 || lon > -122;
    }

    @Override
    double getMinLatForTile(double lat) {
        return 37.5;
    }

    @Override
    double getMinLonForTile(double lon) {
        return -122.25;
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
    public double getEle(double lat, double lon) {
        // Return fast, if there is no data available
        if (isOutsideSupportedArea(lat, lon)) return 0;

        lat = (int) (lat * precision) / precision;
        lon = (int) (lon * precision) / precision;
        String name = getFileName(lat, lon);
        HeightTile demProvider = cacheData.get(name);
        if (demProvider == null) {
            if (!cacheDir.exists()) cacheDir.mkdirs();

            double minLat = getMinLatForTile(lat);
            double minLon = getMinLonForTile(lon);
            // less restrictive against boundary checking
            demProvider = new HeightTile(minLat, minLon, WIDTH, HEIGHT,
                    LON_DEGREE * precision, LON_DEGREE, LAT_DEGREE);
            demProvider.setInterpolate(interpolate);

            cacheData.put(name, demProvider);
            DataAccess heights = getDirectory().create(name + ".gh");
            demProvider.setHeights(heights);
            boolean loadExisting = false;
            try {
                loadExisting = heights.loadExisting();
            } catch (Exception ex) {
                logger.warn("cannot load {}, error: {}", name, ex.getMessage());
            }

            if (!loadExisting) {
                File file = new File(cacheDir,
                        new File(getFileNameOfLocalFile(lat, lon)).getName());

                // short == 2 bytes
                heights.create(2L * WIDTH * HEIGHT);
                Raster raster = generateRasterFromFile(file, name + ".tif");
                super.fillDataAccessWithElevationData(raster, heights, WIDTH);
            } // loadExisting
        }

        if (demProvider.isSeaLevel()) return 0;
        return demProvider.getHeight(lat, lon);
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

    public String toString() { return "usgs"; }
}
