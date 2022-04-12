package com.graphhopper.routing.util.parsers;

import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.OSMJunction;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.EdgeExplorer;

public class OSMJunctionParser implements JunctionCostParser {
  private final DecimalEncodedValue turnCostEnc;
  // private EdgeExplorer cachedOutExplorer, cachedInExplorer;

  public OSMJunctionParser(DecimalEncodedValue turnCostEnc) {
    this.turnCostEnc = turnCostEnc;
  }

  // Is this needed?
  @Override
  public String getName() {
    return turnCostEnc.getName();
  }

  // Is this needed?
  @Override
  public void createTurnCostEncodedValues(EncodedValueLookup lookup, List<EncodedValue> registerNewEncodedValue) {
    registerNewEncodedValue.add(turnCostEnc);

  }

  @Override
  public void handleJunctionTags(OSMJunction junction, ExternalInternalMap map, Graph graph) {
    TurnCostStorage turnCostStorage = graph.getTurnCostStorage();
    boolean isControlled = false;
    // Identify if the junction has a control, such as a stop sign or light

    Integer viaNode = map.getInternalNodeIdOfOsmNode(junction.getJunctionNodeId());
    for (Entry<Integer, ReaderWay> from : junction.getWays().entrySet()) {
      Integer fromEdge = from.getKey();
      ReaderWay fromWay = from.getValue();
      Integer fromLanes;
      try {
        fromLanes = fromWay.hasTag("lanes") ? Integer.parseInt(fromWay.getTag("lanes")) : null;
      } catch (NumberFormatException e) {
        fromLanes = null;
      }
      for (Entry<Integer, ReaderWay> to : junction.getWays().entrySet()) {
        Integer toEdge = to.getKey();
        ReaderWay toWay = to.getValue();
        if (!isControlled) {
          if (toWay.hasTag("highway",
              "motorway", "motorway_link",
              "trunk", "trunk_link",
              "primary", "primary_link",
              "secondary", "secondary_link",
              "tertiary", "tertiary_link")) {
            // DecimalEncodedValue turnCostEnc = TurnCost.create(encoder.toString(),
            // encoder.getMaxTurnCosts());
            turnCostStorage.set(turnCostEnc, fromEdge, viaNode, toEdge, 3d);
          }
        }
        if (!Objects.isNull(fromLanes) && fromLanes > 2) {
          turnCostStorage.set(turnCostEnc, fromEdge, viaNode, toEdge, 3d * fromLanes);
        }
      }
    }
  }

}
