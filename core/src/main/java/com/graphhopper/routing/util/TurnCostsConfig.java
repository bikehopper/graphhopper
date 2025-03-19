package com.graphhopper.routing.util;

public class TurnCostsConfig {
    private double leftCostsSeconds = 8;
    private double leftSharpCostsSeconds = 10;
    private double rightCostsSeconds = 3;
    private double rightSharpCostsSeconds = 5;

    private double uTurnCostsSeconds = 20;

    private double minAngleDegrees = 25, minSharpAngleDegrees = 100, minUTurnAngleDegrees = 180;

    public double getLeftCostsSeconds() {
        return leftCostsSeconds;
    }

    public double getLeftSharpCostsSeconds() {
        return leftSharpCostsSeconds;
    }

    public double getRightCostsSeconds() {
        return rightCostsSeconds;
    }

    public double getRightSharpCostsSeconds() {
        return rightSharpCostsSeconds;
    }

    public double getMinAngleDegrees() {
        return minAngleDegrees;
    }

    public double getMinSharpAngleDegrees() {
        return minSharpAngleDegrees;
    }

    public double getMinUTurnAngleDegrees() {
        return minUTurnAngleDegrees;
    }

    public double getUTurnCostsSeconds() {
        return uTurnCostsSeconds;
    }
}
