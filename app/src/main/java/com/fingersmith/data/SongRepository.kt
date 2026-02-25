package com.fingersmith.data

import android.content.Context
import com.fingersmith.model.Song
import com.fingersmith.model.SongLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SongRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val raw = context.assets.open("songs.json").bufferedReader().use { it.readText() }
        json.decodeFromString<SongLibrary>(raw).songs
    }
}
