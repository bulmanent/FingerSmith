package com.fingersmith.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fingersmith.model.ChordLibrary
import com.fingersmith.model.HandSelection
import com.fingersmith.model.NoteNaming
import com.fingersmith.model.PracticeRamp
import com.fingersmith.model.Song
import com.fingersmith.model.midiToName
import com.fingersmith.ui.MainViewModel
import com.fingersmith.ui.UiState
import com.fingersmith.ui.components.KeyboardCanvas
import com.fingersmith.ui.components.TimelineLane

@Composable
fun SongbookScreen(state: UiState, vm: MainViewModel) {
    var showSongMenu by remember { mutableStateOf(false) }
    var showSectionMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Song Selection")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(
                            enabled = state.songs.isNotEmpty(),
                            onClick = {
                                val next = if (state.selectedSongIndex <= 0) state.songs.lastIndex else state.selectedSongIndex - 1
                                vm.selectSong(next)
                            }
                        ) { Text("Prev") }
                        Text(state.selectedSong?.title ?: "No songs loaded", modifier = Modifier.padding(top = 12.dp))
                        OutlinedButton(
                            enabled = state.songs.isNotEmpty(),
                            onClick = {
                                val next = if (state.selectedSongIndex >= state.songs.lastIndex) 0 else state.selectedSongIndex + 1
                                vm.selectSong(next)
                            }
                        ) { Text("Next") }
                    }
                    OutlinedButton(enabled = state.songs.isNotEmpty(), onClick = { showSongMenu = true }) {
                        Text("Select Song")
                    }
                    DropdownMenu(expanded = showSongMenu, onDismissRequest = { showSongMenu = false }) {
                        state.songs.forEachIndexed { idx, song ->
                            DropdownMenuItem(
                                text = { Text(song.title) },
                                onClick = {
                                    vm.selectSong(idx)
                                    showSongMenu = false
                                }
                            )
                        }
                    }
                    OutlinedButton(enabled = state.songs.isNotEmpty(), onClick = { showSectionMenu = true }) {
                        Text("Loop: ${state.selectedSectionLabel()}")
                    }
                    DropdownMenu(expanded = showSectionMenu, onDismissRequest = { showSectionMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Full Song") },
                            onClick = {
                                vm.selectSection(-1)
                                showSectionMenu = false
                            }
                        )
                        repeat(state.songSectionCount) { index ->
                            DropdownMenuItem(
                                text = { Text("Section ${index + 1}") },
                                onClick = {
                                    vm.selectSection(index)
                                    showSectionMenu = false
                                }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Section Size")
                        FilterChip(
                            selected = state.sectionBars == 8,
                            onClick = { vm.setSectionBars(8) },
                            label = { Text("8") }
                        )
                        FilterChip(
                            selected = state.sectionBars == 16,
                            onClick = { vm.setSectionBars(16) },
                            label = { Text("16") }
                        )
                        FilterChip(
                            selected = state.sectionBars == 32,
                            onClick = { vm.setSectionBars(32) },
                            label = { Text("32") }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.togglePlayPause() }) { Text(if (state.isPlaying) "Pause" else "Play") }
                OutlinedButton(onClick = { vm.stop() }) { Text("Stop") }
                Text("${state.bpm} BPM", modifier = Modifier.padding(top = 12.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.selectedHand == HandSelection.RIGHT, onClick = { vm.setHand(HandSelection.RIGHT) }, label = { Text("Right") })
                FilterChip(selected = state.selectedHand == HandSelection.LEFT, onClick = { vm.setHand(HandSelection.LEFT) }, label = { Text("Left") })
                FilterChip(selected = state.selectedHand == HandSelection.BOTH, onClick = { vm.setHand(HandSelection.BOTH) }, label = { Text("Both") })
            }

            TimelineLane(song = state.selectedSong, currentStep = state.currentStepIndex)
            KeyboardCanvas(
                startMidi = state.currentRange.startMidi,
                keys = state.currentRange.keys,
                activeNotes = state.activeNotes,
                showFingers = state.showFingers,
                showNoteNames = state.showNoteNames,
                onTapMidi = null
            )
            StepAnalysisCard(song = state.selectedSong, currentStep = state.currentStepIndex)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Show Fingers")
                    Switch(checked = state.showFingers, onCheckedChange = vm::setShowFingers)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Note Names")
                    Switch(checked = state.showNoteNames, onCheckedChange = vm::setShowNoteNames)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Piano")
                    Switch(checked = state.pianoEnabled, onCheckedChange = vm::setPianoEnabled)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Metronome")
                    Switch(checked = state.metronomeEnabled, onCheckedChange = vm::setMetronomeEnabled)
                }
            }

            PracticeRampEditor(state.ramp, vm::setRamp)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Speed")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.adjustBpm(-5) }) { Text("-5") }
                    Text("${state.bpm} BPM", modifier = Modifier.padding(top = 12.dp))
                    OutlinedButton(onClick = { vm.resetBpmToSongDefault() }) { Text("Normal") }
                    OutlinedButton(onClick = { vm.adjustBpm(5) }) { Text("+5") }
                }
                Slider(
                    value = state.bpm.toFloat(),
                    onValueChange = { vm.adjustBpm(it.toInt() - state.bpm) },
                    valueRange = 40f..160f
                )
            }
        }
    }
}

private fun UiState.selectedSectionLabel(): String {
    return if (selectedSectionIndex < 0) "Full Song" else "Section ${selectedSectionIndex + 1}"
}

@Composable
private fun StepAnalysisCard(song: Song?, currentStep: Int) {
    var naming by remember { mutableStateOf(NoteNaming.SHARPS) }
    val active = remember(song, currentStep) { song?.activeNotesAtStep(currentStep).orEmpty() }
    val right = remember(active) { active.filter { it.hand == "R" } }
    val left = remember(active) { active.filter { it.hand == "L" } }
    val bothChord = remember(active, naming) { ChordLibrary.detectChord(active.map { it.midi })?.title(naming) ?: "N/A" }
    val rightChord = remember(right, naming) { ChordLibrary.detectChord(right.map { it.midi })?.title(naming) ?: "N/A" }
    val leftChord = remember(left, naming) { ChordLibrary.detectChord(left.map { it.midi })?.title(naming) ?: "N/A" }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Current Step Analysis")
            Text("Step $currentStep")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = naming == NoteNaming.SHARPS, onClick = { naming = NoteNaming.SHARPS }, label = { Text("Sharps") })
                FilterChip(selected = naming == NoteNaming.FLATS, onClick = { naming = NoteNaming.FLATS }, label = { Text("Flats") })
                FilterChip(selected = naming == NoteNaming.ENHARMONIC, onClick = { naming = NoteNaming.ENHARMONIC }, label = { Text("Both") })
            }
            Text("Chord (Both): $bothChord")
            Text("Chord (RH): $rightChord")
            Text("Chord (LH): $leftChord")
            Text("Fingering RH: ${right.toFingerText(naming)}")
            Text("Fingering LH: ${left.toFingerText(naming)}")
        }
    }
}

@Composable
private fun PracticeRampEditor(ramp: PracticeRamp, onUpdate: (PracticeRamp) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Practice Ramp")
                Switch(checked = ramp.enabled, onCheckedChange = { onUpdate(ramp.copy(enabled = it)) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onUpdate(ramp.copy(startBpm = (ramp.startBpm - 1).coerceIn(40, 160))) }) { Text("Start-") }
                Text("Start ${ramp.startBpm}")
                OutlinedButton(onClick = { onUpdate(ramp.copy(startBpm = (ramp.startBpm + 1).coerceIn(40, 160))) }) { Text("Start+") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onUpdate(ramp.copy(endBpm = (ramp.endBpm - 1).coerceIn(40, 160))) }) { Text("End-") }
                Text("End ${ramp.endBpm}")
                OutlinedButton(onClick = { onUpdate(ramp.copy(endBpm = (ramp.endBpm + 1).coerceIn(40, 160))) }) { Text("End+") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onUpdate(ramp.copy(increment = (ramp.increment - 1).coerceIn(1, 20))) }) { Text("Inc-") }
                Text("Inc ${ramp.increment}")
                OutlinedButton(onClick = { onUpdate(ramp.copy(increment = (ramp.increment + 1).coerceIn(1, 20))) }) { Text("Inc+") }
                OutlinedButton(onClick = { onUpdate(ramp.copy(barsPerIncrement = (ramp.barsPerIncrement % 8) + 1)) }) { Text("Bars ${ramp.barsPerIncrement}") }
            }
        }
    }
}

private data class ActiveSongNote(val midi: Int, val finger: Int, val hand: String)

private fun Song.activeNotesAtStep(step: Int): List<ActiveSongNote> {
    return tracks.flatMap { track ->
        val hand = when {
            track.hand.startsWith("L", ignoreCase = true) -> "L"
            else -> "R"
        }
        track.events
            .filter { step >= it.stepIndex && step < (it.stepIndex + it.durationSteps) }
            .flatMap { event ->
                event.notes.map { note -> ActiveSongNote(midi = note.midi, finger = note.finger, hand = hand) }
            }
    }
}

private fun List<ActiveSongNote>.toFingerText(naming: NoteNaming): String {
    if (isEmpty()) return "N/A"
    return sortedBy { it.midi }.joinToString(", ") {
        "${midiToName(it.midi, naming)}(F${it.finger})"
    }
}
