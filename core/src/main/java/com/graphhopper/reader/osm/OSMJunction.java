package com.graphhopper.reader.osm;

import java.util.*;

import com.graphhopper.reader.ReaderWay;

public class OSMJunction {
  private long junctionNodeId;
  private Map<Integer, ReaderWay> ways;

  public OSMJunction(long junctionNodeId) {
    this.junctionNodeId = junctionNodeId;
    this.ways = new HashMap<>();
  }

  public long getJunctionNodeId() {
    return this.junctionNodeId;
  }

  public Map<Integer, ReaderWay> getWays() {
    return this.ways;
  }

  public void addSegment(ReaderWay way, int edgeId) {
    this.ways.put(edgeId, way);
  }
}
