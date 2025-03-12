package com.example.myapp.api;

import java.util.List;

public class CcsSearchRequest {
    private List<CcsRange> ranges;
    
    // Static inner class to represent a single CCS range
    public static class CcsRange {
        private double value;
        private double tolerance;
        
        // Getters and setters
        public double getValue() {
            return value;
        }
        
        public void setValue(double value) {
            this.value = value;
        }
        
        public double getTolerance() {
            return tolerance;
        }
        
        public void setTolerance(double tolerance) {
            this.tolerance = tolerance;
        }
    }
    
    // Getters and setters
    public List<CcsRange> getRanges() {
        return ranges;
    }
    
    public void setRanges(List<CcsRange> ranges) {
        this.ranges = ranges;
    }
}
