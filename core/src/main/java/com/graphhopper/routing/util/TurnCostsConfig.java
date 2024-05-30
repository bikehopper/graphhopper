package com.graphhopper.routing.util;

public class TurnCostsConfig {
    private double leftCost = 45, rightCost = 30, straightCost = 0;
    private double minLeftAngle = 25, maxLeftAngle = 180;
    private double minRightAngle = -25, maxRightAngle = -180;

    private int uTurnCosts = 60;

    public TurnCostsConfig() {}

    public double getLeftCost() {
        return leftCost;
    }

    public double getRightCost() {
        return rightCost;
    }

    public int getUTurnCosts() {
        return uTurnCosts;
    }

    public double getMinLeftAngle() {
        return minLeftAngle;
    }

    public double getMaxLeftAngle() {
        return maxLeftAngle;
    }

    public double getMinRightAngle() {
        return minRightAngle;
    }

    public double getMaxRightAngle() {
        return maxRightAngle;
    }
}
