package com.example.musicplayer.presentation.search

import com.example.musicplayer.data.local.entity.Song

data class SearchState(
    val songs: List<Song> = emptyList(),
    val playingSong: Song? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
