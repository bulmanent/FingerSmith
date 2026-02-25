package com.fingersmith.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlin.math.pow

class PianoSampler(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(24)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val rootToSoundId = mutableMapOf<Int, Int>()
    private val rootNotes = listOf(48, 51, 54, 57, 60, 63, 66, 69, 72, 75, 78, 81, 84)

    init {
        val pkg = context.packageName
        rootNotes.forEach { root ->
            val resName = "pno_$root"
            val resId = context.resources.getIdentifier(resName, "raw", pkg)
            if (resId != 0) {
                rootToSoundId[root] = soundPool.load(context, resId, 1)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun play(midiNote: Int, velocity: Float, durationMs: Int) {
        val root = nearestRoot(midiNote)
        val soundId = rootToSoundId[root] ?: return
        val rate = ((2.0).pow((midiNote - root) / 12.0)).toFloat().coerceIn(0.5f, 2.0f)
        val vol = velocity.coerceIn(0f, 1f)
        soundPool.play(soundId, vol, vol, 1, 0, rate)
    }

    fun stopAll() = soundPool.autoPause()

    fun release() = soundPool.release()

    private fun nearestRoot(target: Int): Int =
        rootNotes.minByOrNull { kotlin.math.abs(it - target) } ?: 60
}
