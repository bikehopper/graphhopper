package com.graphhopper.gtfs;

import java.util.Objects;

public class EdgeVertices {
    private final int baseNode;
    private final int adjNode;

    public EdgeVertices(int baseNode, int adjNode) {
        this.baseNode = baseNode;
        this.adjNode = adjNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EdgeVertices other = (EdgeVertices) o;
        return this.baseNode == other.baseNode && this.adjNode == other.adjNode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseNode, adjNode);
    }

    public int getBaseNode() {
        return baseNode;
    }

    public int getAdjNode() {
        return adjNode;
    }
}
