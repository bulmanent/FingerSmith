package com.fingersmith.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.fingersmith.model.HandSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserSettings(
    val showFingers: Boolean = true,
    val showNoteNames: Boolean = false,
    val pianoEnabled: Boolean = true,
    val metronomeEnabled: Boolean = true,
    val selectedSongTitle: String = "",
    val handSelection: HandSelection = HandSelection.BOTH,
    val lastBpm: Int = 80
)

class SettingsStore(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("settings.preferences_pb")
    }

    private object Keys {
        val SHOW_FINGERS = booleanPreferencesKey("show_fingers")
        val SHOW_NOTE_NAMES = booleanPreferencesKey("show_note_names")
        val PIANO_ENABLED = booleanPreferencesKey("piano_enabled")
        val METRONOME_ENABLED = booleanPreferencesKey("metronome_enabled")
        val SONG_TITLE = stringPreferencesKey("song_title")
        val HAND = stringPreferencesKey("hand")
        val BPM = intPreferencesKey("bpm")
    }

    val settings: Flow<UserSettings> = dataStore.data.map { pref ->
        UserSettings(
            showFingers = pref[Keys.SHOW_FINGERS] ?: true,
            showNoteNames = pref[Keys.SHOW_NOTE_NAMES] ?: false,
            pianoEnabled = pref[Keys.PIANO_ENABLED] ?: true,
            metronomeEnabled = pref[Keys.METRONOME_ENABLED] ?: true,
            selectedSongTitle = pref[Keys.SONG_TITLE] ?: "",
            handSelection = HandSelection.valueOf(pref[Keys.HAND] ?: HandSelection.BOTH.name),
            lastBpm = pref[Keys.BPM] ?: 80
        )
    }

    suspend fun update(transform: suspend (UserSettings) -> UserSettings) {
        dataStore.edit { pref ->
            val current = UserSettings(
                showFingers = pref[Keys.SHOW_FINGERS] ?: true,
                showNoteNames = pref[Keys.SHOW_NOTE_NAMES] ?: false,
                pianoEnabled = pref[Keys.PIANO_ENABLED] ?: true,
                metronomeEnabled = pref[Keys.METRONOME_ENABLED] ?: true,
                selectedSongTitle = pref[Keys.SONG_TITLE] ?: "",
                handSelection = HandSelection.valueOf(pref[Keys.HAND] ?: HandSelection.BOTH.name),
                lastBpm = pref[Keys.BPM] ?: 80
            )
            val updated = transform(current)
            pref[Keys.SHOW_FINGERS] = updated.showFingers
            pref[Keys.SHOW_NOTE_NAMES] = updated.showNoteNames
            pref[Keys.PIANO_ENABLED] = updated.pianoEnabled
            pref[Keys.METRONOME_ENABLED] = updated.metronomeEnabled
            pref[Keys.SONG_TITLE] = updated.selectedSongTitle
            pref[Keys.HAND] = updated.handSelection.name
            pref[Keys.BPM] = updated.lastBpm
        }
    }
}
