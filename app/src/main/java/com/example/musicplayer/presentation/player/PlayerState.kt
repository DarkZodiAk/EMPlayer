package com.example.musicplayer.presentation.player

import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.usecases.RepeatMode

data class PlayerState(
    val playingSong: Song = Song(),
    val isPlaying: Boolean = false,
    val currentProgress: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.NO_REPEAT,
    val isShuffleEnabled: Boolean = false
)
