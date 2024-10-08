package com.example.musicplayer.presentation.home

import com.example.musicplayer.data.local.entity.Song

data class HomeState(
    val playingSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentProgress: Long = 0
)
