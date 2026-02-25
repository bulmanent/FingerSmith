package com.fingersmith.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fingersmith.model.ChordLibrary
import com.fingersmith.model.ChordVoicing
import com.fingersmith.model.NoteNaming
import com.fingersmith.model.ROOT_NAMES
import com.fingersmith.model.rootName
import com.fingersmith.ui.components.KeyboardCanvas

@Composable
fun ChordLibraryScreen() {
    val allChords = remember { ChordLibrary.allChords() }
    var rootFilter by remember { mutableIntStateOf(-1) }
    var query by remember { mutableStateOf("") }
    var selectedKey by remember { mutableStateOf(allChords.firstOrNull()?.stableKey().orEmpty()) }
    var naming by remember { mutableStateOf(NoteNaming.SHARPS) }

    val filtered = remember(rootFilter, query, allChords, naming) {
        allChords.filter { chord ->
            val rootMatches = rootFilter < 0 || chord.rootIndex == rootFilter
            val queryMatches = query.isBlank() ||
                chord.title(naming).contains(query, ignoreCase = true) ||
                chord.noteNames(naming).joinToString(" ").contains(query, ignoreCase = true)
            rootMatches && queryMatches
        }
    }
    val selectedChord = filtered.firstOrNull { it.stableKey() == selectedKey } ?: filtered.firstOrNull() ?: allChords.firstOrNull()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Chord Library (${allChords.size} chords)")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = naming == NoteNaming.SHARPS, onClick = { naming = NoteNaming.SHARPS }, label = { Text("Sharps") })
            FilterChip(selected = naming == NoteNaming.FLATS, onClick = { naming = NoteNaming.FLATS }, label = { Text("Flats") })
            FilterChip(selected = naming == NoteNaming.ENHARMONIC, onClick = { naming = NoteNaming.ENHARMONIC }, label = { Text("Both") })
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = rootFilter == -1, onClick = { rootFilter = -1 }, label = { Text("All Roots") })
            }
            items(ROOT_NAMES.size) { index ->
                FilterChip(
                    selected = rootFilter == index,
                    onClick = { rootFilter = index },
                    label = { Text(rootName(index, naming)) }
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search chord") },
            modifier = Modifier.fillMaxWidth()
        )

        selectedChord?.let { chord ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Selected: ${chord.title(naming)}")
                    Text("Notes: ${chord.noteNames(naming).joinToString(" - ")}")
                }
            }
            KeyboardCanvas(
                startMidi = 48,
                keys = 37,
                activeNotes = chord.midiNotes.associateWith { 1 },
                showFingers = false,
                showNoteNames = true,
                onTapMidi = null
            )
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filtered, key = ChordVoicing::stableKey) { chord ->
                Card(onClick = { selectedKey = chord.stableKey() }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(chord.title(naming))
                        Text(chord.noteNames(naming).joinToString(" "))
                    }
                }
            }
        }
    }
}

private fun ChordVoicing.stableKey(): String = "${rootIndex}_${quality.symbol}_$inversion"
