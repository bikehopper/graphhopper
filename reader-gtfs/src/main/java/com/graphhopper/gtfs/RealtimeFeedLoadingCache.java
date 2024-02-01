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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.transit.realtime.GtfsRealtime;
import com.graphhopper.storage.GraphHopperStorage;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RealtimeFeedLoadingCache{

    private final HttpClient httpClient;
    private final GraphHopperStorage graphHopperStorage;
    private final GtfsStorage gtfsStorage;
    private final List<URI> feedUris;
    private ExecutorService executor;
    private LoadingCache<String, RealtimeFeed> cache;
    private Map<String, Transfers> transfers;

    RealtimeFeedLoadingCache(GraphHopperStorage graphHopperStorage, GtfsStorage gtfsStorage, HttpClient httpClient, List<URI> feedUris) {
        this.graphHopperStorage = graphHopperStorage;
        this.gtfsStorage = gtfsStorage;
        this.feedUris = feedUris;
        this.httpClient = httpClient;
        this.start();
    }

    public void start() {
        this.transfers = new HashMap<>();
        for (Map.Entry<String, GTFSFeed> entry : this.gtfsStorage.getGtfsFeeds().entrySet()) {
            this.transfers.put(entry.getKey(), new Transfers(entry.getValue()));
        }
        this.executor = Executors.newSingleThreadExecutor();
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .refreshAfterWrite(30, TimeUnit.SECONDS)
                .build(new CacheLoader<String, RealtimeFeed>() {
                    public RealtimeFeed load(String key) {
                        return fetchFeedsAndCreateGraph();
                    }

                    @Override
                    public ListenableFuture<RealtimeFeed> reload(String key, RealtimeFeed oldValue) {
                        ListenableFutureTask<RealtimeFeed> task = ListenableFutureTask.create(() -> fetchFeedsAndCreateGraph());
                        executor.execute(task);
                        return task;
                    }
                });
    }

    public RealtimeFeed provide() {
        try {
            return cache.get("pups");
        } catch (ExecutionException | RuntimeException e) {
            e.printStackTrace();
            return RealtimeFeed.empty();
        }
    }

    private RealtimeFeed fetchFeedsAndCreateGraph() {
        Map<String, GtfsRealtime.FeedMessage> feedMessageMap = new HashMap<>();
        for (URI uri: this.feedUris) {
            try {
                GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.parseFrom(httpClient.execute(new HttpGet(uri)).getEntity().getContent());
                feedMessageMap.put("RG", feedMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return RealtimeFeed.fromProtobuf(graphHopperStorage, gtfsStorage, this.transfers, feedMessageMap);
    }

}
