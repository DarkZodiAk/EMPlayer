package com.example.musicplayer.presentation.search

import com.example.musicplayer.data.local.entity.Audio

data class SearchState(
    val songs: List<Audio> = emptyList(),
    val playingSong: Audio? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
