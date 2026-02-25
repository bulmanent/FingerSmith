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
import com.fingersmith.ui.screens.CustomPracticeScreen
import com.fingersmith.ui.screens.SongbookScreen

@Composable
fun AppScreen(vm: MainViewModel) {
    val state by vm.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = state.tab.ordinal) {
            Tab(selected = state.tab == MainTab.SONGBOOK, onClick = { vm.setTab(MainTab.SONGBOOK) }, text = { Text("Songbook") })
            Tab(selected = state.tab == MainTab.CUSTOM, onClick = { vm.setTab(MainTab.CUSTOM) }, text = { Text("Custom Practice") })
        }

        when (state.tab) {
            MainTab.SONGBOOK -> SongbookScreen(state = state, vm = vm)
            MainTab.CUSTOM -> CustomPracticeScreen(state = state, vm = vm)
        }
    }
}
