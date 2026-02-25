package com.fingersmith

import com.fingersmith.util.StepMath
import org.junit.Assert.assertEquals
import org.junit.Test

class StepMathTest {
    @Test
    fun stepDurationAt120bpm() {
        assertEquals(125L, StepMath.stepDurationMs(120))
    }

    @Test
    fun quantizeTempoBounds() {
        assertEquals(40, StepMath.quantizeTempo(10))
        assertEquals(160, StepMath.quantizeTempo(300))
        assertEquals(96, StepMath.quantizeTempo(96))
    }

    @Test
    fun barCalculation() {
        assertEquals(0L, StepMath.barOfStep(0))
        assertEquals(1L, StepMath.barOfStep(16))
        assertEquals(3L, StepMath.barOfStep(63))
    }
}
