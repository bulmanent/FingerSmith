package com.fingersmith.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fingersmith.audio.Metronome
import com.fingersmith.audio.PianoSampler
import com.fingersmith.audio.PlaybackEngine
import com.fingersmith.data.SettingsStore
import com.fingersmith.data.SongRepository
import com.fingersmith.data.UserSettings
import com.fingersmith.model.HandSelection
import com.fingersmith.model.NoteOn
import com.fingersmith.model.PracticeRamp
import com.fingersmith.model.Song
import com.fingersmith.model.SongRange
import com.fingersmith.model.Track
import com.fingersmith.model.Event
import com.fingersmith.model.StepGrid
import com.fingersmith.util.StepMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab { SONGBOOK, CUSTOM }

data class CustomStep(val notes: MutableList<NoteOn> = mutableListOf())

data class UiState(
    val songs: List<Song> = emptyList(),
    val selectedSongIndex: Int = 0,
    val selectedHand: HandSelection = HandSelection.BOTH,
    val bpm: Int = 80,
    val isPlaying: Boolean = false,
    val currentStepIndex: Int = 0,
    val activeNotes: Map<Int, Int> = emptyMap(),
    val showFingers: Boolean = true,
    val showNoteNames: Boolean = false,
    val pianoEnabled: Boolean = true,
    val metronomeEnabled: Boolean = true,
    val ramp: PracticeRamp = PracticeRamp(),
    val tab: MainTab = MainTab.SONGBOOK,
    val customSteps: Map<Int, CustomStep> = emptyMap(),
    val customLoopBars: Int = 4,
    val customSelectedStep: Int = 0,
    val customFinger: Int = 1
) {
    val selectedSong: Song?
        get() = songs.getOrNull(selectedSongIndex)

    val currentRange: SongRange
        get() = selectedSong?.range ?: SongRange()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)
    private val settingsStore = SettingsStore(application)
    private val playbackEngine = PlaybackEngine(PianoSampler(application), Metronome(application))

    private val internal = MutableStateFlow(UiState())
    private val settings = settingsStore.settings.stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val uiState: StateFlow<UiState> = combine(
        internal,
        playbackEngine.currentStepIndex,
        playbackEngine.activeNotes,
        settings
    ) { state, step, active, pref ->
        state.copy(
            currentStepIndex = step,
            activeNotes = active,
            showFingers = pref.showFingers,
            showNoteNames = pref.showNoteNames,
            pianoEnabled = pref.pianoEnabled,
            metronomeEnabled = pref.metronomeEnabled,
            selectedHand = pref.handSelection,
            bpm = pref.lastBpm
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        viewModelScope.launch {
            val songs = songRepository.loadSongs()
            val initialIndex = songs.indexOfFirst { it.title == settings.value.selectedSongTitle }.takeIf { it >= 0 } ?: 0
            internal.value = internal.value.copy(songs = songs, selectedSongIndex = initialIndex)
        }
    }

    fun setTab(tab: MainTab) {
        internal.value = internal.value.copy(tab = tab)
    }

    fun selectSong(index: Int) {
        internal.value = internal.value.copy(selectedSongIndex = index)
        viewModelScope.launch {
            settingsStore.update { it.copy(selectedSongTitle = internal.value.selectedSong?.title ?: "") }
        }
    }

    fun setHand(selection: HandSelection) {
        viewModelScope.launch { settingsStore.update { it.copy(handSelection = selection) } }
    }

    fun adjustBpm(delta: Int) {
        val target = StepMath.quantizeTempo((uiState.value.bpm + delta))
        viewModelScope.launch { settingsStore.update { it.copy(lastBpm = target) } }
    }

    fun setShowFingers(value: Boolean) {
        viewModelScope.launch { settingsStore.update { it.copy(showFingers = value) } }
    }

    fun setShowNoteNames(value: Boolean) {
        viewModelScope.launch { settingsStore.update { it.copy(showNoteNames = value) } }
    }

    fun setPianoEnabled(value: Boolean) {
        viewModelScope.launch { settingsStore.update { it.copy(pianoEnabled = value) } }
    }

    fun setMetronomeEnabled(value: Boolean) {
        viewModelScope.launch { settingsStore.update { it.copy(metronomeEnabled = value) } }
    }

    fun setRamp(ramp: PracticeRamp) {
        internal.value = internal.value.copy(ramp = ramp)
    }

    fun togglePlayPause() {
        val state = uiState.value
        if (state.isPlaying) {
            playbackEngine.pause()
            internal.value = internal.value.copy(isPlaying = false)
            return
        }

        val song = when (state.tab) {
            MainTab.SONGBOOK -> state.selectedSong
            MainTab.CUSTOM -> buildCustomSong(state)
        } ?: return

        playbackEngine.play(
            song = song,
            handSelection = state.selectedHand,
            startBpm = state.bpm,
            ramp = state.ramp,
            pianoOn = state.pianoEnabled,
            metronomeOn = state.metronomeEnabled
        )
        internal.value = internal.value.copy(isPlaying = true)
    }

    fun stop() {
        playbackEngine.stop()
        internal.value = internal.value.copy(isPlaying = false)
    }

    fun selectCustomStep(step: Int) {
        internal.value = internal.value.copy(customSelectedStep = step)
    }

    fun setCustomFinger(finger: Int) {
        internal.value = internal.value.copy(customFinger = finger.coerceIn(1, 5))
    }

    fun setCustomLoopBars(bars: Int) {
        internal.value = internal.value.copy(customLoopBars = bars.coerceIn(1, 16))
    }

    fun addCustomNote(midi: Int) {
        val state = internal.value
        val copy = state.customSteps.toMutableMap()
        val step = copy.getOrPut(state.customSelectedStep) { CustomStep() }
        step.notes += NoteOn(midi = midi, finger = state.customFinger)
        copy[state.customSelectedStep] = step
        internal.value = state.copy(customSteps = copy)
    }

    fun clearCustomStep() {
        val state = internal.value
        val copy = state.customSteps.toMutableMap()
        copy.remove(state.customSelectedStep)
        internal.value = state.copy(customSteps = copy)
    }

    override fun onCleared() {
        super.onCleared()
        playbackEngine.release()
    }

    private fun buildCustomSong(state: UiState): Song {
        val loopSteps = state.customLoopBars * StepGrid.STEPS_PER_BAR
        val events = state.customSteps.entries
            .filter { it.key < loopSteps }
            .sortedBy { it.key }
            .map { (step, custom) ->
                Event(stepIndex = step, durationSteps = 2, notes = custom.notes.toList())
            }

        return Song(
            title = "Custom Practice",
            defaultBpm = state.bpm,
            range = SongRange(),
            tracks = listOf(Track(name = "Custom", hand = "R", events = events))
        )
    }
}
