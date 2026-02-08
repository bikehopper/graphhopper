package com.graphhopper.resources;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

import java.io.IOException;
import java.io.RandomAccessFile;

@Path("/pmtiles")
public class PMTilesResource {

    private static String mimeType = "application/vnd.pmtiles";
    private static String pmtilesPath = "web-bundle/src/main/resources/debug/ways.pmtiles";
    private static final Logger logger = LoggerFactory.getLogger(PMTilesResource.class);
    private static RandomAccessFile file;

    @Inject
    public PMTilesResource(GraphHopper graphHopper, EncodingManager encodingManager) {
        try {
            if (file == null) {
                file = new RandomAccessFile(pmtilesPath, "r");
            }
        } catch (IOException e) {
            logger.warn(pmtilesPath + "file not present");
        }
    }

    @GET
    @Path("/ways.pmtiles")
    public Response getFile( @HeaderParam("Range") String range) throws IOException {
        if (file == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        // Assume full file
        long start = 0;
        long end = file.length() - 1;
        boolean partial = range != null;

        ResponseBuilder res;
        // Narrow down range if its specified
        if (partial) {
            String[] ranges = range.substring("bytes=".length()).split("-");
            start = Long.parseLong(ranges[0]);

            // if end is specified set to it
            if (ranges.length > 1) {
                long potentialEnd = Long.parseLong(ranges[1]);
                if (potentialEnd <= end) {
                    end = potentialEnd;
                }
            }

            res = Response.status(Response.Status.PARTIAL_CONTENT);

        } else {
            res = Response.ok();
        }

        file.seek(start);

        int length = (int) (end - start) + 1;
        byte[] data = new byte[length];

        // Read the data into the buffer
        file.read(data);
        res.entity(data);

        // Set headers
        res.header("Content-Length", length);
        res.header("Content-Type", mimeType);

        if (partial) {
            res.header("Content-Range", "bytes " + start + "-" + end + "/" + file.length());
        } else {
           res.header("Accept-Ranges", "bytes"); // Indicate that the server accepts range requests
        }

        return res.build();
    }
}