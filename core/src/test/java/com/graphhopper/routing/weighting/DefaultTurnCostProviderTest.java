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
import org.junit.jupiter.api.Test;

public class DefaultTurnCostProviderTest {
    public DefaultTurnCostProvider turnCostProvider;

    /**
     * Checks that DefaultTurnCostProvider.calcTurnWeight() returns the supplied
     * u-turn cost when given the same in- and out-edges.
     */
    @Test
    public void testEqualEdgeUTurnCost() {
        DecimalEncodedValue orientation = Orientation.create();
        TurnCostsConfig config = new TurnCostsConfig();
        turnCostProvider = new DefaultTurnCostProvider(orientation, null, config);
        assertEquals(config.getUTurnCostsSeconds(),
                turnCostProvider.calcTurnWeight(1, 2, 1));
    }

    /**
     * Checks that a straight angle between two edges is awarded no turn cost.
     */
    @Test
    public void testStraightNoCost() {
        DecimalEncodedValue orientation = Orientation.create();
        OrientationCalculator calculator =
                new OrientationCalculator(orientation);
        EncodingManager em = new EncodingManager.Builder()
                .add(orientation).build();
        BaseGraph graph = new BaseGraph.Builder(em)
                .withTurnCosts(true).create();

        TurnCostProvider turnCostProvider =
                new DefaultTurnCostProvider(orientation, graph,
                        new TurnCostsConfig());

        // 0 - 1 - 2
        graph.getNodeAccess().setNode(0, 37.760843, -122.436103);
        graph.getNodeAccess().setNode(1, 37.760905, -122.435038);
        graph.getNodeAccess().setNode(2, 37.760975, -122.433937);
        List<Double> pts = new ArrayList<>();
        EdgeIteratorState edge01 = handleWayTags(calculator, graph.edge(0, 1),
                pts);
        EdgeIteratorState edge02 = handleWayTags(calculator, graph.edge(1, 2),
                pts);

        assertEquals(0, turnCostProvider.calcTurnWeight(edge01.getEdge(), 1,
                edge02.getEdge()));
    }

    EdgeIteratorState handleWayTags(OrientationCalculator calc, EdgeIteratorState edge, List<Double> rawPoints) {
        if (rawPoints.size() % 2 != 0)
            throw new IllegalArgumentException();
        if (!rawPoints.isEmpty()) {
            PointList pointList = new PointList();
            for (int i = 0; i < rawPoints.size(); i += 2) {
                pointList.add(rawPoints.get(0), rawPoints.get(1));
            }
            edge.setWayGeometry(pointList);
        }
        ReaderWay way = new ReaderWay(1);
        way.setTag("point_list", edge.fetchWayGeometry(FetchMode.ALL));
        calc.handleWayTags(edge.getFlags(), way, null);
        return edge;
    }
}
