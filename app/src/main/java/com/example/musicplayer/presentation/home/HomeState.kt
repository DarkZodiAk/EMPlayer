package com.example.musicplayer.presentation.home

import com.example.musicplayer.data.local.entity.Audio

data class HomeState(
    val playingSong: Audio? = null,
    val isPlaying: Boolean = false,
    val currentProgress: Long = 0
)
