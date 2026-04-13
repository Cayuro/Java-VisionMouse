package com.vision.gesture;

public class EMAFilter {
    private double alpha;
    private double previousValue;

    public EMAFilter(double alpha) {
        this.alpha = alpha;
        this.previousValue = Double.NaN; // First call passes through
    }

    public double filter(double newValue) {
        if (Double.isNaN(previousValue)) {
            previousValue = newValue;
        } else {
            previousValue = alpha * newValue + (1.0 - alpha) * previousValue;
        }
        return previousValue;
    }
}
