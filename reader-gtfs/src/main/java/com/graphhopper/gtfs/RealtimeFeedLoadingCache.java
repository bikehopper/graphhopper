package com.graphhopper.gtfs;

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

import com.conveyal.gtfs.GTFSFeed;
import com.google.transit.realtime.GtfsRealtime;
import com.graphhopper.storage.GraphHopperStorage;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RealtimeFeedLoadingCache{

    private final HttpClient httpClient;
    private final GraphHopperStorage graphHopperStorage;
    private final GtfsStorage gtfsStorage;
    private final List<URI> feedUris;
    private Map<String, Transfers> transfers;
    private RealtimeFeed latestFeed;

    RealtimeFeedLoadingCache(GraphHopperStorage graphHopperStorage, GtfsStorage gtfsStorage, List<String> feedUris) {
        this.graphHopperStorage = graphHopperStorage;
        this.gtfsStorage = gtfsStorage;
        this.feedUris = new ArrayList<>();
        for(String feedUri: feedUris){
            try {
                URI uri = new URI(feedUri);
                this.feedUris.add(uri);
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.httpClient = new DefaultHttpClient();
        this.start();
    }

    public void start() {
        this.transfers = new HashMap<>();
        for (Map.Entry<String, GTFSFeed> entry : this.gtfsStorage.getGtfsFeeds().entrySet()) {
            this.transfers.put(entry.getKey(), new Transfers(entry.getValue()));
        }
        this.latestFeed = fetchFeedsAndCreateGraph();
        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                latestFeed = fetchFeedsAndCreateGraph();
            }
        },0,120000);
    }

    public RealtimeFeed provide() {
        if(latestFeed == null) {
            return RealtimeFeed.empty();
        } else {
            return latestFeed;
        }
    }

    private RealtimeFeed fetchFeedsAndCreateGraph() {
        Map<String, GtfsRealtime.FeedMessage> feedMessageMap = new HashMap<>();
        for (URI uri: this.feedUris) {
            try {
                GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.parseFrom(httpClient.execute(new HttpGet(uri)).getEntity().getContent());
                feedMessageMap.put("gtfs_0", feedMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return RealtimeFeed.fromProtobuf(graphHopperStorage, gtfsStorage, this.transfers, feedMessageMap);
    }

}
