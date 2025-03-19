package com.graphhopper.routing.weighting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Orientation;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TurnCostsConfig;
import com.graphhopper.routing.util.parsers.OrientationCalculator;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultTurnCostProviderTest {

    private DecimalEncodedValue orientationEnc;
    private OrientationCalculator orientationCalculator;
    private EncodingManager encodingManager;
    private BaseGraph graph;
    private TurnCostProvider turnCostProvider;
    private TurnCostsConfig turnCostsConfig;

    @BeforeEach
    public void setup() {
        encodingManager = new EncodingManager.Builder().add(Orientation.create()).build();
        orientationEnc = encodingManager.getDecimalEncodedValue(Orientation.KEY);
        orientationCalculator = new OrientationCalculator(orientationEnc);

        // Left: 8, Sharp Left: 10
        // Right: 3, Sharp Right: 5
        // U-Turn: 20
        turnCostsConfig = new TurnCostsConfig();

        graph = new BaseGraph.Builder(encodingManager).withTurnCosts(true)
                .create();

        turnCostProvider = new DefaultTurnCostProvider(orientationEnc, graph, turnCostsConfig);
    }

    /**
     * Checks that DefaultTurnCostProvider.calcTurnWeight() returns the supplied
     * u-turn cost when given the same in- and out-edges.
     */
    @Test
    public void testEqualEdgeUTurnCost() {
        DecimalEncodedValue orientation = Orientation.create();
        TurnCostsConfig config = new TurnCostsConfig();
        turnCostProvider = new DefaultTurnCostProvider(orientation, null,
                config);
        assertEquals(config.getUTurnCostsSeconds(),
                turnCostProvider.calcTurnWeight(1, 2, 1));
    }

    /**
     * Checks that a straight angle between two edges is awarded no turn cost.
     */
    @Test
    public void testStraightNoCost() {
        // 0 - 1 - 2
        int sourceNode = 0, viaNode = 1, targetNode = 2;
        graph.getNodeAccess().setNode(sourceNode, 37.760843, -122.436103);
        graph.getNodeAccess().setNode(viaNode, 37.760905, -122.435038);
        graph.getNodeAccess().setNode(targetNode, 37.760975, -122.433937);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(orientationCalculator,
                graph.edge(sourceNode, viaNode), pts);
        EdgeIteratorState edge12 = handleWayTags(orientationCalculator,
                graph.edge(viaNode, targetNode), pts);

        assertEquals(0,
                turnCostProvider.calcTurnWeight(edge01.getEdge(), viaNode,
                        edge12.getEdge()));
    }

    @Test
    public void testLeftTurnCost() {
        //      2
        //      |
        // 0 -- 1
        int sourceNode = 0, viaNode = 1, targetNode = 2;
        graph.getNodeAccess().setNode(sourceNode, 37.76083, -122.43613);
        graph.getNodeAccess().setNode(viaNode, 37.7609, -122.43503);
        graph.getNodeAccess().setNode(targetNode, 37.7625, -122.43519);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(orientationCalculator,
                graph.edge(sourceNode, viaNode), pts);
        EdgeIteratorState edge12 = handleWayTags(orientationCalculator,
                graph.edge(viaNode, targetNode), pts);

        assertEquals(turnCostsConfig.getLeftCostsSeconds(),
                turnCostProvider.calcTurnWeight(edge01.getEdge(), viaNode,
                        edge12.getEdge()));
    }

    @Test
    public void testRightTurnCost() {
        // 0 -- 1
        //      |
        //      2
        int sourceNode = 0, viaNode = 1, targetNode = 2;
        graph.getNodeAccess().setNode(sourceNode, 37.760843, -122.436103);
        graph.getNodeAccess().setNode(viaNode, 37.760905, -122.435038);
        graph.getNodeAccess().setNode(targetNode, 37.759321, -122.434888);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(orientationCalculator,
                graph.edge(sourceNode, viaNode), pts);
        EdgeIteratorState edge12 = handleWayTags(orientationCalculator,
                graph.edge(viaNode, targetNode), pts);

        assertEquals(turnCostsConfig.getRightCostsSeconds(),
                turnCostProvider.calcTurnWeight(edge01.getEdge(), viaNode,
                        edge12.getEdge()));
    }

    @Test
    public void testSharpLeftTurnCost() {
        //    2
        //     \
        // 0 -- 1
        int sourceNode = 0, viaNode = 1, targetNode = 2;
        graph.getNodeAccess().setNode(sourceNode, 37.76262, -122.43295);
        graph.getNodeAccess().setNode(viaNode, 37.7641, -122.43313);
        graph.getNodeAccess().setNode(targetNode, 37.76264, -122.43516);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(orientationCalculator,
                graph.edge(sourceNode, viaNode), pts);
        EdgeIteratorState edge12 = handleWayTags(orientationCalculator,
                graph.edge(viaNode, targetNode), pts);

        assertEquals(
                turnCostsConfig.getLeftSharpCostsSeconds(),
                turnCostProvider.calcTurnWeight(edge01.getEdge(), viaNode,
                        edge12.getEdge()));
    }

    @Test
    public void testSharpRightTurnCost() {
        // 0 -- 1
        //     /
        //    2
        int sourceNode = 0, viaNode = 1, targetNode = 2;
        graph.getNodeAccess().setNode(sourceNode, 37.761, -122.44431);
        graph.getNodeAccess().setNode(viaNode, 37.76056, -122.44437);
        graph.getNodeAccess().setNode(targetNode, 37.7612, -122.444627);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(orientationCalculator,
                graph.edge(sourceNode, viaNode), pts);
        EdgeIteratorState edge12 = handleWayTags(orientationCalculator,
                graph.edge(viaNode, targetNode), pts);

        assertEquals(
                turnCostsConfig.getRightSharpCostsSeconds(),
                turnCostProvider.calcTurnWeight(edge01.getEdge(), viaNode,
                        edge12.getEdge()));
    }

    /**
     * Processes way tags using an OrientationCalculator to determine the geometry and flags
     * for a given edge. Converts raw geographical points into a PointList and sets it as the
     * edge's geometry. Updates the edge's flags based on the provided orientation calculator.
     *
     * @param calc the OrientationCalculator used to process way tags
     * @param edge the edge whose geometry and flags are to be updated
     * @param rawPoints a list of raw geographical points represented as [lat, lon] pairs
     * @return the updated EdgeIteratorState with modified geometry and flags
     * @throws IllegalArgumentException if rawPoints size is not even
     */
    EdgeIteratorState handleWayTags(OrientationCalculator calc,
            EdgeIteratorState edge, List<Double> rawPoints) {
        if (rawPoints.size() % 2 != 0)
            throw new IllegalArgumentException();
        if (!rawPoints.isEmpty()) {
            PointList pointList = new PointList();
            for (int i = 0; i < rawPoints.size(); i += 2)
                pointList.add(rawPoints.get(0), rawPoints.get(1));
            edge.setWayGeometry(pointList);
        }
        ReaderWay way = new ReaderWay(1);
        way.setTag("point_list", edge.fetchWayGeometry(FetchMode.ALL));
        edge.setFlags(calc.handleWayTags(edge.getFlags(), way, null));
        return edge;
    }
}
