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
        graph.getNodeAccess().setNode(0, 37.760843, -122.436103);
        graph.getNodeAccess().setNode(1, 37.760905, -122.435038);
        graph.getNodeAccess().setNode(2, 37.760975, -122.433937);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(orientationCalculator,
                graph.edge(0, 1),
                pts);
        EdgeIteratorState edge12 = handleWayTags(orientationCalculator,
                graph.edge(1, 2),
                pts);

        assertEquals(0, turnCostProvider.calcTurnWeight(edge01.getEdge(), 1,
                edge12.getEdge()));
    }

    @Test
    public void testLeftTurnCost() {
        //      2
        //      |
        // 0 -- 1
        int viaNode = 1;
        graph.getNodeAccess().setNode(0, 37.76083, -122.43613);
        graph.getNodeAccess().setNode(viaNode, 37.7609, -122.43503);
        graph.getNodeAccess().setNode(2, 37.7625, -122.43519);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(orientationCalculator, graph.edge(0, viaNode), pts);
        EdgeIteratorState edge12 = handleWayTags(orientationCalculator, graph.edge(viaNode, 2), pts);

        assertEquals(turnCostsConfig.getLeftCostsSeconds(),
                turnCostProvider.calcTurnWeight(edge01.getEdge(), viaNode,
                        edge12.getEdge()));
    }

    @Test
    public void testRightTurnCost() {
        // 0 -- 1
        //      |
        //      2
        int viaNode = 1;
        graph.getNodeAccess().setNode(0, 37.760843, -122.436103);
        graph.getNodeAccess().setNode(viaNode, 37.760905, -122.435038);
        graph.getNodeAccess().setNode(2, 37.759321, -122.434888);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(orientationCalculator, graph.edge(0, viaNode), pts);
        EdgeIteratorState edge12 = handleWayTags(orientationCalculator, graph.edge(viaNode, 2), pts);

        assertEquals(turnCostsConfig.getRightCostsSeconds(),
                turnCostProvider.calcTurnWeight(edge01.getEdge(), viaNode,
                        edge12.getEdge()));
    }

    EdgeIteratorState handleWayTags(OrientationCalculator calc,
            EdgeIteratorState edge, List<Double> rawPoints) {
        if (rawPoints.size() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        if (!rawPoints.isEmpty()) {
            PointList pointList = new PointList();
            for (int i = 0; i < rawPoints.size(); i += 2) {
                pointList.add(rawPoints.get(0), rawPoints.get(1));
            }
            edge.setWayGeometry(pointList);
        }
        ReaderWay way = new ReaderWay(1);
        way.setTag("point_list", edge.fetchWayGeometry(FetchMode.ALL));
        edge.setFlags(calc.handleWayTags(edge.getFlags(), way, null));
        return edge;
    }
}
