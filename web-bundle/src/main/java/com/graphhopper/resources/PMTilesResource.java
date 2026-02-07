package com.graphhopper.resources;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/pmtiles")
public class PMTilesResource {

    private static String mimeType = "application/vnd.pmtiles";
    private static String pmtilesPath = "web-bundle/src/main/resources/debug/ways.pmtiles";
    private static final Logger logger = LoggerFactory.getLogger(PMTilesResource.class);
    private File file;
    private byte[] data;

    @Inject
    public PMTilesResource(GraphHopper graphHopper, EncodingManager encodingManager) {
        this.file = new File(pmtilesPath);
        try {
            this.data = Files.readAllBytes(Paths.get(this.file.getAbsolutePath()));
        } catch (IOException e) {
            logger.warn(pmtilesPath + "file not present");
        }
    }

    @GET
    @Path("/ways.pmtiles")
    public Response getFile( @HeaderParam("Range") String range) throws IOException {
        if (!this.file.exists() || this.data == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (range == null) {
            // Return the full file if no range header is provided
            ResponseBuilder response = Response.ok((Object) this.file);
            response.header("Content-Type", mimeType);
            response.header("Content-Length", this.file.length());
            response.header("Accept-Ranges", "bytes"); // Indicate that the server accepts range requests
            return response.build();
        } else {
            // Handle the range request
            try {
                String[] ranges = range.substring("bytes=".length()).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 ? Long.parseLong(ranges[1]) : this.file.length() - 1;

                if (start > this.file.length() || end >= this.file.length()) {
                     // 416 Requested Range Not Satisfiable
                    return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .header("Content-Range", "bytes */" + file.length())
                            .build();
                }

                // Read the specified byte range from the file
                
                byte[] partialData = new byte[(int) (end - start + 1)];
                System.arraycopy(data, (int) start, partialData, 0, partialData.length);

                // Return partial content (HTTP 206)
                ResponseBuilder response = Response.status(Response.Status.PARTIAL_CONTENT)
                        .entity(partialData)
                        .header("Content-Range", "bytes " + start + "-" + end + "/" + file.length())
                        .header("Content-Length", partialData.length)
                        .header("Content-Type", mimeType); // Set correct MIME type
                return response.build();
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }
        }
    }
}