package com.fingersmith.audio

import com.fingersmith.model.Event
import com.fingersmith.model.HandSelection
import com.fingersmith.model.PracticeRamp
import com.fingersmith.model.Song
import com.fingersmith.model.StepGrid
import com.fingersmith.util.StepMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackEngine(
    private val sampler: PianoSampler,
    private val metronome: Metronome
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _activeNotes = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val activeNotes: StateFlow<Map<Int, Int>> = _activeNotes.asStateFlow()

    private var cursorStep: Long = 0

    fun play(
        song: Song,
        handSelection: HandSelection,
        startBpm: Int,
        ramp: PracticeRamp,
        pianoOn: Boolean,
        metronomeOn: Boolean
    ) {
        if (playbackJob?.isActive == true) return

        val eventsByStep = song.filteredEvents(handSelection).groupBy { it.stepIndex }
        val loopSteps = ((eventsByStep.keys.maxOrNull() ?: 0) + StepGrid.STEPS_PER_BAR).coerceAtLeast(StepGrid.STEPS_PER_BAR)
        var bpm = StepMath.quantizeTempo(if (ramp.enabled) ramp.startBpm else startBpm)
        var stepMs = StepMath.stepDurationMs(bpm).toDouble()
        var scheduledStep = cursorStep
        var nextStepTimeMs = 0.0
        var lastReportedStep = -1L
        val noteEndMs = mutableMapOf<Int, Double>()
        var barsSinceRamp = 0

        playbackJob = scope.launch {
            val startNs = System.nanoTime()
            while (isActive) {
                val nowMs = (System.nanoTime() - startNs) / 1_000_000.0
                val lookAheadMs = nowMs + 120.0

                while (nextStepTimeMs <= lookAheadMs) {
                    val loopStep = (scheduledStep % loopSteps).toInt()
                    val events = eventsByStep[loopStep].orEmpty()
                    events.forEach { event ->
                        event.notes.forEach { note ->
                            if (pianoOn) sampler.play(note.midi, 0.95f, (event.durationSteps * stepMs).toInt())
                            noteEndMs[note.midi] = nextStepTimeMs + (event.durationSteps * stepMs)
                        }
                    }
                    if (metronomeOn && scheduledStep % StepGrid.STEPS_PER_BEAT == 0L) {
                        val beatInBar = (scheduledStep / StepGrid.STEPS_PER_BEAT) % 4
                        metronome.tick(accent = beatInBar == 0L)
                    }
                    scheduledStep++
                    nextStepTimeMs += stepMs

                    if (ramp.enabled && scheduledStep % StepGrid.STEPS_PER_BAR == 0L) {
                        barsSinceRamp++
                        if (barsSinceRamp >= ramp.barsPerIncrement) {
                            barsSinceRamp = 0
                            val next = (bpm + ramp.increment).coerceAtMost(ramp.endBpm)
                            if (next != bpm) {
                                bpm = next
                                stepMs = StepMath.stepDurationMs(bpm).toDouble()
                            }
                        }
                    }
                }

                val currentStep = (nowMs / stepMs).toLong() + cursorStep
                val loopStep = (currentStep % loopSteps).toInt()
                if (currentStep != lastReportedStep) {
                    _currentStepIndex.value = loopStep
                    lastReportedStep = currentStep
                }

                val active = noteEndMs.filterValues { it > nowMs }.keys.associateWith { midi ->
                    val finger = eventsByStep[loopStep]
                        .orEmpty()
                        .flatMap { it.notes }
                        .firstOrNull { it.midi == midi }
                        ?.finger ?: 0
                    finger
                }
                _activeNotes.value = active
                delay(8)
            }
        }
    }

    fun pause() {
        playbackJob?.cancel()
        playbackJob = null
        cursorStep = _currentStepIndex.value.toLong()
    }

    fun stop() {
        pause()
        sampler.stopAll()
        cursorStep = 0
        _currentStepIndex.value = 0
        _activeNotes.value = emptyMap()
    }

    fun release() {
        stop()
        scope.cancel()
        sampler.release()
        metronome.release()
    }

    private fun Song.filteredEvents(handSelection: HandSelection): List<Event> {
        return tracks.filter {
            when (handSelection) {
                HandSelection.RIGHT -> it.hand == "R"
                HandSelection.LEFT -> it.hand == "L"
                HandSelection.BOTH -> true
            }
        }.flatMap { it.events }
    }
}
