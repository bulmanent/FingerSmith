package com.fingersmith.model

data class ChordQuality(
    val symbol: String,
    val intervals: List<Int>
)

enum class NoteNaming { SHARPS, FLATS, ENHARMONIC }

data class ChordVoicing(
    val rootIndex: Int,
    val quality: ChordQuality,
    val inversion: Int,
    val midiNotes: List<Int>,
) {
    fun title(naming: NoteNaming): String {
        val inversionLabel = if (inversion == 0) "" else " (inv $inversion)"
        return "${rootName(rootIndex, naming)} ${quality.symbol}$inversionLabel"
    }

    fun noteNames(naming: NoteNaming): List<String> = midiNotes.map { midiToName(it, naming) }
}

private val CHORD_QUALITIES = listOf(
    ChordQuality("maj", listOf(0, 4, 7)),
    ChordQuality("min", listOf(0, 3, 7)),
    ChordQuality("dim", listOf(0, 3, 6)),
    ChordQuality("aug", listOf(0, 4, 8)),
    ChordQuality("sus2", listOf(0, 2, 7)),
    ChordQuality("sus4", listOf(0, 5, 7)),
    ChordQuality("5", listOf(0, 7)),
    ChordQuality("6", listOf(0, 4, 7, 9)),
    ChordQuality("m6", listOf(0, 3, 7, 9)),
    ChordQuality("7", listOf(0, 4, 7, 10)),
    ChordQuality("maj7", listOf(0, 4, 7, 11)),
    ChordQuality("m7", listOf(0, 3, 7, 10)),
    ChordQuality("mMaj7", listOf(0, 3, 7, 11)),
    ChordQuality("dim7", listOf(0, 3, 6, 9)),
    ChordQuality("m7b5", listOf(0, 3, 6, 10)),
    ChordQuality("7sus4", listOf(0, 5, 7, 10)),
    ChordQuality("add9", listOf(0, 4, 7, 14)),
    ChordQuality("madd9", listOf(0, 3, 7, 14)),
    ChordQuality("6/9", listOf(0, 4, 7, 9, 14)),
    ChordQuality("9", listOf(0, 4, 7, 10, 14)),
    ChordQuality("maj9", listOf(0, 4, 7, 11, 14)),
    ChordQuality("m9", listOf(0, 3, 7, 10, 14)),
    ChordQuality("11", listOf(0, 4, 7, 10, 14, 17)),
    ChordQuality("maj11", listOf(0, 4, 7, 11, 14, 17)),
    ChordQuality("m11", listOf(0, 3, 7, 10, 14, 17)),
    ChordQuality("13", listOf(0, 4, 7, 10, 14, 17, 21)),
    ChordQuality("maj13", listOf(0, 4, 7, 11, 14, 17, 21)),
    ChordQuality("m13", listOf(0, 3, 7, 10, 14, 17, 21)),
    ChordQuality("7b5", listOf(0, 4, 6, 10)),
    ChordQuality("7#5", listOf(0, 4, 8, 10)),
    ChordQuality("7b9", listOf(0, 4, 7, 10, 13)),
    ChordQuality("7#9", listOf(0, 4, 7, 10, 15)),
    ChordQuality("7#11", listOf(0, 4, 7, 10, 18)),
    ChordQuality("7b13", listOf(0, 4, 7, 10, 20)),
    ChordQuality("maj7#11", listOf(0, 4, 7, 11, 18)),
    ChordQuality("maj7b5", listOf(0, 4, 6, 11)),
    ChordQuality("m7b9", listOf(0, 3, 7, 10, 13)),
    ChordQuality("m7add11", listOf(0, 3, 7, 10, 17))
)

val ROOT_NAMES_SHARP = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
val ROOT_NAMES_FLAT = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")
val ROOT_NAMES = ROOT_NAMES_SHARP

object ChordLibrary {
    private val detectionCatalog: List<ChordVoicing> by lazy { allChords(baseOctave = 3) }

    fun qualities(): List<ChordQuality> = CHORD_QUALITIES

    fun allChords(baseOctave: Int = 4): List<ChordVoicing> {
        return ROOT_NAMES.indices.flatMap { root ->
            CHORD_QUALITIES.flatMap { quality ->
                val rootPosition = chord(root, quality, 0, baseOctave)
                listOf(rootPosition) + (1 until rootPosition.midiNotes.size).map { inversion ->
                    chord(root, quality, inversion, baseOctave)
                }
            }
        }
    }

    fun chord(rootIndex: Int, quality: ChordQuality, inversion: Int = 0, baseOctave: Int = 4): ChordVoicing {
        val rootMidi = (baseOctave + 1) * 12 + rootIndex
        val baseNotes = quality.intervals.map { rootMidi + it }
        val safeInversion = inversion.coerceIn(0, (baseNotes.size - 1).coerceAtLeast(0))
        val notes = baseNotes.mapIndexed { index, midi ->
            if (index < safeInversion) midi + 12 else midi
        }.sorted()
        return ChordVoicing(
            rootIndex = rootIndex,
            quality = quality,
            inversion = safeInversion,
            midiNotes = notes
        )
    }

    fun detectChord(notes: List<Int>): ChordVoicing? {
        if (notes.isEmpty()) return null
        val noteSet = notes.map { ((it % 12) + 12) % 12 }.toSet()
        return detectionCatalog.firstOrNull { chord ->
            chord.midiNotes.map { ((it % 12) + 12) % 12 }.toSet() == noteSet
        }
    }
}

fun rootName(rootIndex: Int, naming: NoteNaming): String {
    val normalized = ((rootIndex % 12) + 12) % 12
    return when (naming) {
        NoteNaming.SHARPS -> ROOT_NAMES_SHARP[normalized]
        NoteNaming.FLATS -> ROOT_NAMES_FLAT[normalized]
        NoteNaming.ENHARMONIC -> enharmonicName(normalized)
    }
}

fun midiToName(midi: Int, naming: NoteNaming): String {
    val pitchClass = ((midi % 12) + 12) % 12
    return rootName(pitchClass, naming)
}

private fun enharmonicName(pitchClass: Int): String {
    val sharp = ROOT_NAMES_SHARP[pitchClass]
    val flat = ROOT_NAMES_FLAT[pitchClass]
    return if (sharp == flat) sharp else "$sharp/$flat"
}
