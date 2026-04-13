package com.mouseVision.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.vision.gesture.EMAFilter;

public class EMAFilterTest {

    @Test
    void shouldFilterValuesCorrectly() {
        EMAFilter filter = new EMAFilter(0.3);
        
        double filtered = filter.filter(10.0);
        
        // First value passes through unchanged
        assertEquals(10.0, filtered, 0.001);
    }

    @Test
    void shouldApplyEMASmoothing() {
        EMAFilter filter = new EMAFilter(0.3);
        
        filter.filter(10.0);
        double filtered = filter.filter(20.0);
        
        // EMA formula: 0.3*20 + 0.7*10 = 13.0
        assertEquals(13.0, filtered, 0.001);
    }

    @Test
    void shouldMaintainStateAcrossCalls() {
        EMAFilter filter = new EMAFilter(0.5);
        
        filter.filter(100.0);
        filter.filter(0.0);
        double filtered = filter.filter(50.0);
        
        // Step by step: 
        // 100 -> 100
        // 0 -> 50
        // 50 -> 50
        assertEquals(50.0, filtered, 0.001);
    }
}

