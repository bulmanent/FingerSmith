package com.fingersmith.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fingersmith.ui.MainViewModel
import com.fingersmith.ui.UiState
import com.fingersmith.ui.components.KeyboardCanvas

@Composable
fun CustomPracticeScreen(state: UiState, vm: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Step ${state.customSelectedStep}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.selectCustomStep((state.customSelectedStep - 1).coerceAtLeast(0)) }) { Text("Prev") }
                    OutlinedButton(onClick = { vm.selectCustomStep(state.customSelectedStep + 1) }) { Text("Next") }
                    OutlinedButton(onClick = { vm.clearCustomStep() }) { Text("Clear Step") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { finger ->
                        FilterChip(selected = state.customFinger == finger, onClick = { vm.setCustomFinger(finger) }, label = { Text("F$finger") })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.setCustomLoopBars(state.customLoopBars - 1) }) { Text("Bars-") }
                    Text("Loop Bars ${state.customLoopBars}")
                    OutlinedButton(onClick = { vm.setCustomLoopBars(state.customLoopBars + 1) }) { Text("Bars+") }
                }
            }
        }

        KeyboardCanvas(
            startMidi = 48,
            keys = 37,
            activeNotes = state.activeNotes,
            showFingers = true,
            showNoteNames = state.showNoteNames,
            onTapMidi = vm::addCustomNote
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.togglePlayPause() }) { Text(if (state.isPlaying) "Pause" else "Play") }
            OutlinedButton(onClick = { vm.stop() }) { Text("Stop") }
            OutlinedButton(onClick = { vm.adjustBpm(-5) }) { Text("-5") }
            OutlinedButton(onClick = { vm.adjustBpm(5) }) { Text("+5") }
            Text("${state.bpm} BPM", modifier = Modifier.padding(top = 12.dp))
        }
    }
}
