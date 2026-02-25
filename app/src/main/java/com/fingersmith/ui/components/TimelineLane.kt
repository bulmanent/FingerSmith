package com.fingersmith.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fingersmith.model.Song
import com.fingersmith.model.StepGrid

@Composable
fun TimelineLane(song: Song?, currentStep: Int) {
    Canvas(modifier = Modifier.fillMaxWidth().height(68.dp)) {
        drawRect(Color(0xFF1A2432))
        if (song == null) return@Canvas

        val events = song.tracks.flatMap { it.events }
        val totalSteps = (events.maxOfOrNull { it.stepIndex + it.durationSteps } ?: StepGrid.STEPS_PER_BAR).coerceAtLeast(StepGrid.STEPS_PER_BAR)
        val pxPerStep = size.width / totalSteps

        events.forEach { ev ->
            val x = ev.stepIndex * pxPerStep
            val w = ev.durationSteps * pxPerStep
            drawRect(
                color = Color(0xFF4EC5FF),
                topLeft = Offset(x, 12f),
                size = androidx.compose.ui.geometry.Size(w.coerceAtLeast(2f), 44f)
            )
        }

        val playX = currentStep * pxPerStep
        drawLine(
            color = Color(0xFFFFC76B),
            start = Offset(playX, 0f),
            end = Offset(playX, size.height),
            strokeWidth = 4f
        )
    }
}
