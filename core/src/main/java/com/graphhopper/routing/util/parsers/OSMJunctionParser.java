package com.graphhopper.routing.util.parsers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import com.carrotsearch.hppc.cursors.LongCursor;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.OSMJunction;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.TurnCostStorage;

public class OSMJunctionParser implements JunctionCostParser {
  private final DecimalEncodedValue turnCostEnc;

  public OSMJunctionParser(DecimalEncodedValue turnCostEnc) {
    this.turnCostEnc = turnCostEnc;
  }

  @Override
  public String getName() {
    return turnCostEnc.getName();
  }

  @Override
  public void createTurnCostEncodedValues(EncodedValueLookup lookup, List<EncodedValue> registerNewEncodedValue) {
    registerNewEncodedValue.add(turnCostEnc);

  }

  @Override
  public void handleJunctionTags(OSMJunction junction, ExternalInternalMap map, Graph graph) {
    Integer viaNode = map.getInternalNodeIdOfOsmNode(junction.getJunctionNodeId());
    boolean isControlled = isControlled(map, junction);
    if (junction.getJunctionNodeId() == 531239900 || junction.getJunctionNodeId() == 694066163) {
      System.out.println("hello");
    }

    for (Entry<Integer, ReaderWay> from : junction.getWays().entrySet()) {
      Integer fromEdge = from.getKey();
      ReaderWay fromWay = from.getValue();
      Integer fromLanes = getWayLanes(fromWay);

      for (Entry<Integer, ReaderWay> to : junction.getWays().entrySet()) {
        Integer toEdge = to.getKey();
        ReaderWay toWay = to.getValue();

        // If a junction is uncontrolled and features a large way that is not the
        // current way, add a cost to encourage the router to find a controlled
        // intersection
        if (!isControlled && toWay.hasTag("highway",
              "motorway", "motorway_link",
              "trunk", "trunk_link",
              "primary", "primary_link",
              "secondary", "secondary_link",
              "tertiary", "tertiary_link")) {
          storeInTurnCostStorage(graph, fromEdge, viaNode, toEdge, 100d);
        }

        // If this turn involves crossing the direction of travel, add a cost related to
        // the number of lanes needed to cross
        if (!Objects.isNull(fromLanes) && fromLanes > 2) {
          storeInTurnCostStorage(graph, fromEdge, viaNode, toEdge, 10d * fromLanes);
        }
      }
    }
  }

  boolean isControlled(ExternalInternalMap map, OSMJunction junction) {
    if (isControlled(map, junction.getJunctionNodeId()))
      return true;

    for (ReaderWay way : junction.getWays().values()) {
      if (!isControlled(map, way))
        return false;
    }

    return true;
  }

  boolean isControlled(ExternalInternalMap map, ReaderWay way) {
    for (LongCursor cursor : way.getNodes()) {
      if (isControlled(map, cursor.value))
        return true;
    }
    return false;
  }

  boolean isControlled(ExternalInternalMap map, long nodeId) {
    Map<String, Object> tags = map.getNodeTagsOfOsmNode(nodeId); // TODO: for some reason this does not work
    return Arrays.asList("stop", "traffic_signals").contains(tags.get("highway"));
  }

  Integer getWayLanes(ReaderWay way) {
    try {
      return way.hasTag("lanes") ? Integer.parseInt(way.getTag("lanes")) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  void storeInTurnCostStorage(Graph graph, int fromEdge, int viaNode, int toEdge, double cost) {
    TurnCostStorage turnCostStorage = graph.getTurnCostStorage();

    cost = Math.min(cost, turnCostEnc.getMaxDecimal());

    turnCostStorage.set(turnCostEnc, fromEdge, viaNode, toEdge, cost);
  }

}
