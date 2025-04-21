package com.example.musicplayer.presentation.search

import com.example.musicplayer.data.local.entity.Song

data class SearchState(
    val songs: List<Song> = emptyList(),
    val playingSong: Song? = null,
    val searchQuery: String = "",

    // Indicates, if device should automatically show keyboard.
    // If there's return to the SearchScreen from PlayerScreen, don't show keyboard
    val firstVisit: Boolean = true
)
