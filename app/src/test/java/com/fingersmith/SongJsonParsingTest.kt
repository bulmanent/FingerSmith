package com.fingersmith

import com.fingersmith.model.SongLibrary
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SongJsonParsingTest {
    @Test
    fun parsesLibrarySchema() {
        val input = """
            {
              "songs": [
                {
                  "title": "Test Song",
                  "defaultBpm": 90,
                  "range": {"startMidi": 48, "keys": 37},
                  "tracks": [
                    {
                      "name": "RH",
                      "hand": "R",
                      "events": [
                        {"stepIndex": 0, "durationSteps": 2, "notes": [{"midi": 60, "finger": 1}]}
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val parsed = Json.decodeFromString<SongLibrary>(input)
        assertEquals(1, parsed.songs.size)
        assertEquals("Test Song", parsed.songs.first().title)
        assertEquals(60, parsed.songs.first().tracks.first().events.first().notes.first().midi)
    }
}
