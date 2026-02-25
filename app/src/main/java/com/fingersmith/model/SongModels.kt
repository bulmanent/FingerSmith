package com.fingersmith.model

import kotlinx.serialization.Serializable

@Serializable
data class SongLibrary(
    val songs: List<Song>
)

@Serializable
data class Song(
    val title: String,
    val defaultBpm: Int,
    val range: SongRange = SongRange(),
    val tracks: List<Track>
)

@Serializable
data class SongRange(
    val startMidi: Int = 48,
    val keys: Int = 37
)

@Serializable
data class Track(
    val name: String,
    val hand: String,
    val events: List<Event>
)

@Serializable
data class Event(
    val stepIndex: Int,
    val durationSteps: Int,
    val notes: List<NoteOn>
)

@Serializable
data class NoteOn(
    val midi: Int,
    val finger: Int
)

enum class HandSelection { RIGHT, LEFT, BOTH }

data class PracticeRamp(
    val enabled: Boolean = false,
    val startBpm: Int = 60,
    val endBpm: Int = 100,
    val increment: Int = 4,
    val barsPerIncrement: Int = 2
)

object StepGrid {
    const val TIME_SIGNATURE_TOP = 4
    const val STEPS_PER_BAR = 16
    const val STEPS_PER_BEAT = 4
}
