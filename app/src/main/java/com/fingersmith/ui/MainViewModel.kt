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
import com.fingersmith.model.PracticeRamp
import com.fingersmith.model.Song
import com.fingersmith.model.SongRange
import com.fingersmith.model.StepGrid
import com.fingersmith.util.StepMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab { SONGBOOK, CHORD_LIBRARY }

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
    val sectionBars: Int = 16,
    val selectedSectionIndex: Int = 0,
    val tab: MainTab = MainTab.SONGBOOK
) {
    val selectedSong: Song?
        get() = songs.getOrNull(selectedSongIndex)

    val currentRange: SongRange
        get() = selectedSong?.range ?: SongRange()

    val songSectionCount: Int
        get() {
            val song = selectedSong ?: return 1
            val totalSteps = (song.tracks.flatMap { it.events }.maxOfOrNull { it.stepIndex + it.durationSteps }
                ?: StepGrid.STEPS_PER_BAR).coerceAtLeast(StepGrid.STEPS_PER_BAR)
            val sectionSteps = sectionBars * StepGrid.STEPS_PER_BAR
            return ((totalSteps + sectionSteps - 1) / sectionSteps).coerceAtLeast(1)
        }
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
        internal.value = internal.value.copy(selectedSongIndex = index, selectedSectionIndex = 0)
        viewModelScope.launch {
            settingsStore.update { it.copy(selectedSongTitle = internal.value.selectedSong?.title ?: "") }
        }
    }

    fun selectSection(index: Int) {
        val state = internal.value
        val maxSection = (state.songSectionCount - 1).coerceAtLeast(0)
        val clamped = index.coerceIn(-1, maxSection)
        internal.value = state.copy(selectedSectionIndex = clamped)
        if (uiState.value.isPlaying) stop()
    }

    fun setSectionBars(bars: Int) {
        val safeBars = when (bars) {
            8, 16, 32 -> bars
            else -> 16
        }
        val current = internal.value
        val updated = current.copy(sectionBars = safeBars)
        val maxSection = (updated.songSectionCount - 1).coerceAtLeast(0)
        val clampedSection = updated.selectedSectionIndex.coerceIn(-1, maxSection)
        internal.value = updated.copy(selectedSectionIndex = clampedSection)
        if (uiState.value.isPlaying) stop()
    }

    fun setHand(selection: HandSelection) {
        viewModelScope.launch { settingsStore.update { it.copy(handSelection = selection) } }
    }

    fun adjustBpm(delta: Int) {
        val target = StepMath.quantizeTempo((uiState.value.bpm + delta))
        viewModelScope.launch { settingsStore.update { it.copy(lastBpm = target) } }
    }

    fun resetBpmToSongDefault() {
        val target = uiState.value.selectedSong?.defaultBpm ?: 80
        viewModelScope.launch { settingsStore.update { it.copy(lastBpm = StepMath.quantizeTempo(target)) } }
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

        val song = state.selectedSong ?: return
        val totalSteps = song.totalSteps()
        val sectionSteps = state.sectionBars * StepGrid.STEPS_PER_BAR
        val (loopStartStep, loopLengthSteps) = if (state.selectedSectionIndex < 0) {
            0 to totalSteps
        } else {
            val start = (state.selectedSectionIndex * sectionSteps).coerceAtMost((totalSteps - 1).coerceAtLeast(0))
            val length = (totalSteps - start).coerceAtMost(sectionSteps).coerceAtLeast(StepGrid.STEPS_PER_BAR)
            start to length
        }

        playbackEngine.play(
            song = song,
            handSelection = state.selectedHand,
            startBpm = state.bpm,
            ramp = state.ramp,
            pianoOn = state.pianoEnabled,
            metronomeOn = state.metronomeEnabled,
            loopStartStep = loopStartStep,
            loopLengthSteps = loopLengthSteps
        )
        internal.value = internal.value.copy(isPlaying = true)
    }

    fun stop() {
        playbackEngine.stop()
        internal.value = internal.value.copy(isPlaying = false)
    }

    override fun onCleared() {
        super.onCleared()
        playbackEngine.release()
    }

    private fun Song.totalSteps(): Int {
        return (tracks.flatMap { it.events }.maxOfOrNull { it.stepIndex + it.durationSteps }
            ?: StepGrid.STEPS_PER_BAR).coerceAtLeast(StepGrid.STEPS_PER_BAR)
    }
}
