/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.resources;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphhopper.GHResponse;
import com.graphhopper.gtfs.GHLocation;
import com.graphhopper.gtfs.PtRouter;
import com.graphhopper.gtfs.Request;
import com.graphhopper.http.DurationParam;
import com.graphhopper.http.GHLocationParam;
import com.graphhopper.http.OffsetDateTimeParam;
import com.graphhopper.jackson.ResponsePathSerializer;
import com.graphhopper.util.Helper;
import com.graphhopper.util.StopWatch;
import io.dropwizard.jersey.params.AbstractParam;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Path("route-pt")
public class PtRouteResource {

    private final PtRouter ptRouter;

    @Inject
    public PtRouteResource(PtRouter ptRouter) {
        this.ptRouter = ptRouter;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectNode route(@QueryParam("point") @Size(min=2,max=2) List<GHLocationParam> requestPoints,
                            @QueryParam("pt.earliest_departure_time") @NotNull OffsetDateTimeParam departureTimeParam,
                            @QueryParam("pt.profile_duration") DurationParam profileDuration,
                            @QueryParam("pt.arrive_by") @DefaultValue("false") boolean arriveBy,
                            @QueryParam("locale") String localeStr,
                            @QueryParam("pt.ignore_transfers") Boolean ignoreTransfers,
                            @QueryParam("pt.profile") Boolean profileQuery,
                            @QueryParam("pt.limit_solutions") Integer limitSolutions,
                            @QueryParam("pt.limit_trip_time") DurationParam limitTripTime,
                            @QueryParam("pt.limit_street_time") DurationParam limitStreetTime,
                            @QueryParam("pt.connecting_profile") String connectingProfile,
                            @QueryParam("pt.beta_transfers") Double betaTransfers,
                            @QueryParam("pt.beta_street_time") Double betaStreetTime,
                            @QueryParam("pt.beta_pt_edge_time") Double betaPtEdgeTime,
                            @QueryParam("elevation") @DefaultValue("false") boolean enableElevation,
                            @QueryParam("include_edges") @DefaultValue("false") boolean includeEdges,
                            @QueryParam("details") List<String> pathDetails,
                            @QueryParam("pt.block_route_types") List<Integer> blockRouteTypes) {
        StopWatch stopWatch = new StopWatch().start();
        List<GHLocation> points = requestPoints.stream().map(AbstractParam::get).collect(toList());
        Instant departureTime = departureTimeParam.get().toInstant();

        Request request = new Request(points, departureTime);
        request.setArriveBy(arriveBy);
        Optional.ofNullable(profileQuery).ifPresent(request::setProfileQuery);
        Optional.ofNullable(profileDuration.get()).ifPresent(request::setMaxProfileDuration);
        Optional.ofNullable(ignoreTransfers).ifPresent(request::setIgnoreTransfers);
        Optional.ofNullable(localeStr).ifPresent(s -> request.setLocale(Helper.getLocale(s)));
        Optional.ofNullable(limitSolutions).ifPresent(request::setLimitSolutions);
        Optional.ofNullable(limitTripTime.get()).ifPresent(request::setLimitTripTime);
        Optional.ofNullable(limitStreetTime.get()).ifPresent(request::setLimitStreetTime);
        Optional.ofNullable(connectingProfile).ifPresent(request::setConnectingProfile);
        Optional.ofNullable(betaTransfers).ifPresent(request::setBetaTransfers);
        Optional.ofNullable(betaStreetTime).ifPresent(request::setBetaStreetTime);
        Optional.ofNullable(enableElevation).ifPresent(request::setEnableElevation);
        Optional.ofNullable(includeEdges).ifPresent(request::setIncludeEdges);
        Optional.ofNullable(pathDetails).ifPresent(request::setPathDetails);

        if (blockRouteTypes != null) {
          int blockedRouteTypesBitflag = 0;
          for (Integer routeType : blockRouteTypes) {
            // Sanity check by making sure the route type fits in a bitflag, which
            // it should for all valid GTFS route types.
            if (routeType >= 0 && routeType < 32) {
              blockedRouteTypesBitflag = blockedRouteTypesBitflag | (1 << routeType);
            }
          }
          request.setBlockedRouteTypes(blockedRouteTypesBitflag);
        }

        GHResponse route = ptRouter.route(request);
        return ResponsePathSerializer.jsonObject(route, true, includeEdges, true, enableElevation, false, stopWatch.stop().getMillis());
    }

}
