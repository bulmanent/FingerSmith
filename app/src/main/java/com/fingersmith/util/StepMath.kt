package com.fingersmith.util

import com.fingersmith.model.StepGrid
import kotlin.math.max

object StepMath {
    fun stepDurationMs(bpm: Int): Long {
        val safe = max(40, bpm)
        return (60_000.0 / safe / StepGrid.STEPS_PER_BEAT).toLong()
    }

    fun barOfStep(stepIndex: Long): Long = stepIndex / StepGrid.STEPS_PER_BAR

    fun quantizeTempo(value: Int): Int = value.coerceIn(40, 160)
}
