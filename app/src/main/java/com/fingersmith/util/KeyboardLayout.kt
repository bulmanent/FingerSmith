package com.fingersmith.util

import androidx.compose.ui.geometry.Rect

data class KeyRenderInfo(
    val midi: Int,
    val isBlack: Boolean,
    val rect: Rect,
    val label: String
)

object KeyboardLayout {
    private val blackPitchClasses = setOf(1, 3, 6, 8, 10)
    private val labels = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun isBlack(midi: Int): Boolean = blackPitchClasses.contains(midi % 12)

    fun noteName(midi: Int): String {
        val octave = (midi / 12) - 1
        return labels[midi % 12] + octave
    }

    fun midiToKeyIndex(midi: Int, startMidi: Int, keys: Int): Int {
        val idx = midi - startMidi
        return if (idx in 0 until keys) idx else -1
    }

    fun buildLayout(startMidi: Int, keys: Int, width: Float, height: Float): List<KeyRenderInfo> {
        val all = (0 until keys).map { startMidi + it }
        val whites = all.filterNot(::isBlack)
        val whiteWidth = width / whites.size.coerceAtLeast(1)
        val blackWidth = whiteWidth * 0.62f
        val blackHeight = height * 0.62f

        val whiteX = mutableMapOf<Int, Float>()
        whites.forEachIndexed { idx, midi -> whiteX[midi] = idx * whiteWidth }

        return all.map { midi ->
            if (!isBlack(midi)) {
                KeyRenderInfo(
                    midi = midi,
                    isBlack = false,
                    rect = Rect(whiteX.getValue(midi), 0f, whiteX.getValue(midi) + whiteWidth, height),
                    label = noteName(midi)
                )
            } else {
                val leftWhite = midi - 1
                val x = (whiteX[leftWhite] ?: 0f) + whiteWidth - (blackWidth / 2f)
                KeyRenderInfo(
                    midi = midi,
                    isBlack = true,
                    rect = Rect(x, 0f, x + blackWidth, blackHeight),
                    label = noteName(midi)
                )
            }
        }
    }
}
