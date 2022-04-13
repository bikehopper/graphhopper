package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.osm.OSMJunction;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.storage.Graph;

import java.util.List;
import java.util.Map;

public interface JunctionCostParser {
  String getName();

  void createTurnCostEncodedValues(EncodedValueLookup lookup, List<EncodedValue> registerNewEncodedValue);

  void handleJunctionTags(OSMJunction junction, ExternalInternalMap map, Graph graph);

  /**
   * This map associates the internal GraphHopper nodes IDs with external IDs
   * (OSM) and similarly for the edge IDs
   * required to write the turn costs. Returns -1 if there is no entry for the
   * given OSM ID.
   */
  interface ExternalInternalMap {
    int getInternalNodeIdOfOsmNode(long nodeOsmId);

    long getOsmIdOfInternalEdge(int edgeId);

    Map<String, Object> getNodeTagsOfOsmNode(long nodeOsmId);
  }
}
