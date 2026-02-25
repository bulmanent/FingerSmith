package com.fingersmith.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fingersmith.ui.screens.ChordLibraryScreen
import com.fingersmith.ui.screens.SongbookScreen

@Composable
fun AppScreen(vm: MainViewModel) {
    val state by vm.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = state.tab.ordinal) {
            Tab(selected = state.tab == MainTab.SONGBOOK, onClick = { vm.setTab(MainTab.SONGBOOK) }, text = { Text("Songbook") })
            Tab(selected = state.tab == MainTab.CHORD_LIBRARY, onClick = { vm.setTab(MainTab.CHORD_LIBRARY) }, text = { Text("Chord Library") })
        }

        when (state.tab) {
            MainTab.SONGBOOK -> SongbookScreen(state = state, vm = vm)
            MainTab.CHORD_LIBRARY -> ChordLibraryScreen()
        }
    }
}
