package com.graphhopper.routing.util.parsers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.OSMJunction;
import com.graphhopper.reader.osm.SegmentNode;
import com.graphhopper.reader.osm.OSMJunction.JunctionPart;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.AngleCalc;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import com.graphhopper.routing.InstructionsHelper;

public class OSMJunctionParser implements JunctionCostParser {
  private final DecimalEncodedValue turnCostEnc;
  private static final Set<String> restrictedValues = new HashSet<>();
  private static final Map<String, Double> highwayTurnCosts = new HashMap<>();

  public OSMJunctionParser(DecimalEncodedValue turnCostEnc) {
    this.turnCostEnc = turnCostEnc;

    restrictedValues.add("agricultural");
    restrictedValues.add("forestry");
    restrictedValues.add("no");
    restrictedValues.add("restricted");
    restrictedValues.add("delivery");
    restrictedValues.add("military");
    restrictedValues.add("emergency");
    restrictedValues.add("private");
    restrictedValues.add("destination");

    // highwayTurnCosts.put("tertiary", 30 * 1000d);
    // highwayTurnCosts.put("tertiary_link", 30 * 1000d);
    highwayTurnCosts.put("secondary", 60 * 1000d);
    highwayTurnCosts.put("secondary_link", 60 * 1000d);
    highwayTurnCosts.put("primary", 120 * 1000d);
    highwayTurnCosts.put("primary_link", 120 * 1000d);
    highwayTurnCosts.put("trunk", 240 * 1000d);
    highwayTurnCosts.put("trunk_link", 240 * 1000d);
    highwayTurnCosts.put("motorway_link", 480 * 1000d);
    highwayTurnCosts.put("motorway", 480 * 1000d);
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
    SegmentNode viaNode = junction.getJunctionNode();
    boolean isControlled = isControlled(map, viaNode.osmNodeId);

    for (JunctionPart from : junction.getParts()) {
      for (JunctionPart to : junction.getParts()) {

        if (from.edgeId == to.edgeId) {
          // u turn costs are handled elsewhere
          continue;
        }

        // If a turn is against traffic (e.g. left in right-driving countries), add a
        // cost
        int sign = getTurnSign(map, viaNode, from, to);
        double cost = 0d;
        if (sign <= Instruction.TURN_LEFT) {
          // The 'to' and 'from' streets can contribute to turn costs
          if (!isControlled) {
            cost = getTurnCost(junction);
          }
        }
        // If a turn is straight through, and the junction is uncontrolled, add a cost
        else if (sign >= Instruction.TURN_SLIGHT_LEFT && sign <= Instruction.TURN_SLIGHT_RIGHT) {
          if (!isControlled) {
            // The 'to' street and 'from' street don't contribute to costs
            cost = getTurnCost(junction, from, to);
          }
        }
        if (cost != 0)
          storeInTurnCostStorage(graph, from.edgeId, viaNode.id, to.edgeId, cost);
      }
    }
  }

  double getTurnCost(OSMJunction junction, JunctionPart... ignoreParts) {
    List<JunctionPart> ignoreList = Arrays.asList(ignoreParts);
    double cost = 0;
    for (JunctionPart part : junction.getParts()) {
      if (isLargeHighway(part.way) && !ignoreList.contains(part)) {
        Double thisCost = highwayTurnCosts.get(part.way.getTag("highway"));
        if (!Objects.isNull(thisCost))
          cost = Math.max(cost, thisCost);
      }
    }
    return cost;
  }

  int getTurnSign(ExternalInternalMap map, SegmentNode viaNode, JunctionPart from, JunctionPart to) {
    GHPoint3D viaPoint = map.getPointOfOsmNode(viaNode.osmNodeId);
    GHPoint3D fromPoint = getPointNextTo(map, viaPoint, from.points);
    GHPoint3D toPoint = getPointNextTo(map, viaPoint, to.points);
    if (fromPoint == null)
      fromPoint = viaPoint;
    if (toPoint == null)
      toPoint = viaPoint;
    return getSign(map, fromPoint, viaPoint, toPoint);
  }

  // Retrieve the nearest point to a junction node
  GHPoint3D getPointNextTo(ExternalInternalMap map, GHPoint3D viaPoint, PointList points) {
    OptionalInt maybeInt = IntStream.range(0, points.size())
        .filter(i -> points.get(i).equals(viaPoint))
        .findFirst();
    if (!maybeInt.isPresent())
      return null;
    int viaPointIndex = maybeInt.getAsInt();

    int step = 1;
    int direction = viaPointIndex == 0 ? 1 : -1;
    return points.get(viaPointIndex + direction * step);
  }

  // A simplified version of InstructionsFromEdges#getTurn
  int getSign(ExternalInternalMap map, GHPoint3D fromPoint,
      GHPoint3D viaPoint, GHPoint3D toPoint) {

    double prevOrientation = AngleCalc.ANGLE_CALC.calcOrientation(fromPoint.lat, fromPoint.lon,
        viaPoint.lat, viaPoint.lon);
    return InstructionsHelper.calculateSign(viaPoint.lat, viaPoint.lon, toPoint.lat, toPoint.lon, prevOrientation);
  }

  boolean isLargeHighway(ReaderWay way) {
    return way.hasTag("highway",
        highwayTurnCosts.keySet())
        && !way.hasTag("motor_vehicle", restrictedValues);
  }

  boolean isControlled(ExternalInternalMap map, long nodeId) {
    Map<String, Object> tags = map.getNodeTagsOfOsmNode(nodeId);
    return Arrays.asList("stop", "traffic_signals").contains(tags.get("highway"));
  }

  void storeInTurnCostStorage(Graph graph, int fromEdge, int viaNode, int toEdge, double cost) {
    TurnCostStorage turnCostStorage = graph.getTurnCostStorage();

    cost = Math.min(cost, turnCostEnc.getMaxDecimal());
    turnCostStorage.set(turnCostEnc, fromEdge, viaNode, toEdge, cost);
  }

}
