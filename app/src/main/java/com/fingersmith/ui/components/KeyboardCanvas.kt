package com.fingersmith.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.fingersmith.util.KeyboardLayout

@Composable
fun KeyboardCanvas(
    startMidi: Int,
    keys: Int,
    activeNotes: Map<Int, Int>,
    showFingers: Boolean,
    showNoteNames: Boolean,
    onTapMidi: ((Int) -> Unit)? = null
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(Color(0xFF10131A))
            .pointerInput(onTapMidi) {
                detectTapGestures { offset ->
                    if (onTapMidi == null) return@detectTapGestures
                    val layout = KeyboardLayout.buildLayout(
                        startMidi = startMidi,
                        keys = keys,
                        width = size.width.toFloat(),
                        height = size.height.toFloat()
                    )
                    val hit = layout
                        .filter { it.isBlack }
                        .firstOrNull { it.rect.contains(offset) }
                        ?: layout.filter { !it.isBlack }.firstOrNull { it.rect.contains(offset) }
                    hit?.let { onTapMidi(it.midi) }
                }
            }
    ) {
        val layout = KeyboardLayout.buildLayout(startMidi, keys, size.width, size.height)
        val whiteKeys = layout.filter { !it.isBlack }
        val blackKeys = layout.filter { it.isBlack }

        whiteKeys.forEach { key ->
            val active = activeNotes.containsKey(key.midi)
            drawRect(
                color = if (active) Color(0xFF85FFE8) else Color(0xFFF4F7FF),
                topLeft = key.rect.topLeft,
                size = key.rect.size,
                style = Fill
            )
            drawRect(Color.Black.copy(alpha = 0.4f), key.rect.topLeft, key.rect.size, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
        }

        blackKeys.forEach { key ->
            val active = activeNotes.containsKey(key.midi)
            drawRect(
                color = if (active) Color(0xFFFFA86B) else Color(0xFF151515),
                topLeft = key.rect.topLeft,
                size = key.rect.size,
                style = Fill
            )
        }

        layout.forEach { key ->
            val finger = activeNotes[key.midi]
            if (showFingers && finger != null && finger in 1..5) {
                drawContext.canvas.nativeCanvas.drawText(
                    finger.toString(),
                    key.rect.center.x,
                    if (key.isBlack) key.rect.bottom - 10f else key.rect.bottom - 16f,
                    android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = if (key.isBlack) 28f else 32f
                        color = android.graphics.Color.WHITE
                        isFakeBoldText = true
                    }
                )
            }
            if (showNoteNames && !key.isBlack) {
                drawContext.canvas.nativeCanvas.drawText(
                    key.label,
                    key.rect.center.x,
                    key.rect.top + 26f,
                    android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 20f
                        color = android.graphics.Color.DKGRAY
                    }
                )
            }
        }
    }
}
