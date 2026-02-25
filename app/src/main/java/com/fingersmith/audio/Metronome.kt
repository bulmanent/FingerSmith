package com.fingersmith.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class Metronome(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val hi: Int = soundPool.load(context, context.resources.getIdentifier("click_hi", "raw", context.packageName), 1)
    private val lo: Int = soundPool.load(context, context.resources.getIdentifier("click_lo", "raw", context.packageName), 1)

    fun tick(accent: Boolean) {
        val soundId = if (accent) hi else lo
        soundPool.play(soundId, 0.75f, 0.75f, 1, 0, 1f)
    }

    fun release() = soundPool.release()
}
