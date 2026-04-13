package com.mousevision.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EMAFilterTest {

    @Test
    void shouldReturnSameValueOnFirstApply() {
        EMAFilter filter = new EMAFilter(0.5);

        double result = filter.apply(10.0);

        assertEquals(10.0, result, 0.001);
    }

    @Test
    void shouldSmoothValuesCorrectly() {
        EMAFilter filter = new EMAFilter(0.5);

        filter.apply(10.0); // inicial
        double result = filter.apply(20.0);

        // EMA = 0.5 * 20 + 0.5 * 10 = 15
        assertEquals(15.0, result, 0.001);
    }

    @Test
    void shouldConvergeOverMultipleValues() {
        EMAFilter filter = new EMAFilter(0.2);

        filter.apply(0);
        double result = filter.apply(10);

        assertTrue(result > 0 && result < 10);
    }
}