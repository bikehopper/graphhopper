package com.graphhopper.routing.util;

public class TurnCostsConfig {
    private double leftCostsSeconds = 45;
    private double leftSharpCostsSeconds = 45;
    private double rightCostsSeconds = 5;
    private double rightSharpCostsSeconds = 15;

    private double uTurnCostsSeconds = 60;

    private double minAngleDegrees = 25, minSharpAngleDegrees = 80, minUTurnAngleDegrees = 180;

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
