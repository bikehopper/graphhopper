package com.graphhopper.routing.util.parsers;

import java.util.Arrays;
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
    if (viaNode.osmNodeId == 53111744 || viaNode.id == 16095) { // Manila: 53111744 / 21914; Lawton-College: 53080702 /
                                                                // 16095
      System.out.println(viaNode);
    }

    for (JunctionPart from : junction.getParts()) {
      Integer fromLanes = getWayLanes(from.way);

      for (JunctionPart to : junction.getParts()) {

        if (from.edgeId == to.edgeId) {
          continue;
        }

        // If a turn is uncontrolled and features a large way, add a cost to encourage
        // the router to find a controlled intersection -- make cost progressively
        // harsh?
        if (!isControlled && isTraffickedMotorWay(to.way)) {
          storeInTurnCostStorage(graph, from.edgeId, viaNode.id, to.edgeId, 60d * 5);
        }

        // If this turn involves crossing the direction of travel, add a cost related to
        // the number of lanes needed to cross -- or fall back to highway tag
        GHPoint3D viaPoint = map.getPointOfOsmNode(viaNode.osmNodeId);
        GHPoint3D fromPoint = getPointNextTo(map, viaPoint, from.points);
        GHPoint3D toPoint = getPointNextTo(map, viaPoint, to.points);
        if (fromPoint == null)
          fromPoint = viaPoint;
        if (toPoint == null)
          toPoint = viaPoint;
        int sign = getSign(map, fromPoint, viaPoint, toPoint);

        if (sign <= Instruction.TURN_LEFT && !Objects.isNull(fromLanes) && fromLanes > 1) {
          storeInTurnCostStorage(graph, from.edgeId, viaNode.id, to.edgeId, 60d * fromLanes);
        }
      }
    }
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

  boolean isTraffickedMotorWay(ReaderWay way) {
    return way.hasTag("highway",
        "motorway", "motorway_link",
        "trunk", "trunk_link",
        "primary", "primary_link",
        "secondary", "secondary_link",
        "tertiary", "tertiary_link")
        && !way.hasTag("motor_vehicle", restrictedValues);
  }

  boolean isControlled(ExternalInternalMap map, long nodeId) {
    Map<String, Object> tags = map.getNodeTagsOfOsmNode(nodeId);
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
    if (viaNode < 0) {
      System.out.println("h");
    }
    turnCostStorage.set(turnCostEnc, fromEdge, viaNode, toEdge, cost);
  }

}
