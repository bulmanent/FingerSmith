package com.fingersmith.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fingersmith.model.HandSelection
import com.fingersmith.model.PracticeRamp
import com.fingersmith.ui.MainViewModel
import com.fingersmith.ui.UiState
import com.fingersmith.ui.components.KeyboardCanvas
import com.fingersmith.ui.components.TimelineLane

@Composable
fun SongbookScreen(state: UiState, vm: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Songs")
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(state.songs) { idx, song ->
                Card(onClick = { vm.selectSong(idx) }) {
                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(song.title)
                        if (idx == state.selectedSongIndex) Text("Selected")
                    }
                }
            }
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.togglePlayPause() }) { Text(if (state.isPlaying) "Pause" else "Play") }
            OutlinedButton(onClick = { vm.stop() }) { Text("Stop") }
            OutlinedButton(onClick = { vm.adjustBpm(-5) }) { Text("-5") }
            OutlinedButton(onClick = { vm.adjustBpm(-1) }) { Text("-1") }
            Text("${state.bpm} BPM", modifier = Modifier.padding(top = 12.dp))
            OutlinedButton(onClick = { vm.adjustBpm(1) }) { Text("+1") }
            OutlinedButton(onClick = { vm.adjustBpm(5) }) { Text("+5") }
        }

        Slider(
            value = state.bpm.toFloat(),
            onValueChange = { vm.adjustBpm(it.toInt() - state.bpm) },
            valueRange = 40f..160f
        )

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
