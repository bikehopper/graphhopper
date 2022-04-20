package com.graphhopper.reader.osm;

import java.util.*;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.PointList;

public class OSMJunction {
  private SegmentNode junctionNode;
  private List<JunctionPart> parts;

  public OSMJunction(SegmentNode junctionNode) {
    this.junctionNode = junctionNode;
    this.parts = new ArrayList<>();
  }

  public SegmentNode getJunctionNode() {
    return this.junctionNode;
  }

  public List<JunctionPart> getParts() {
    return this.parts;
  }

  public void addSegment(ReaderWay way, PointList points, int edgeId, List<SegmentNode> segment) {
    this.parts.add(new JunctionPart(way, points, edgeId, segment));
  }

  public void setJunctionNodeId(Integer value) {
    this.junctionNode.id = value;
  }

  public class JunctionPart {
    public ReaderWay way;
    public PointList points;
    public Integer edgeId;
    public List<SegmentNode> segment;

    public JunctionPart(ReaderWay way, PointList points, int edgeId, List<SegmentNode> segment) {
      this.way = way;
      this.points = points;
      this.edgeId = edgeId;
      this.segment = segment;
    }
  }
}
