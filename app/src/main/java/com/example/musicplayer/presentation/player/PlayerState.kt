package com.example.musicplayer.presentation.player

import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.domain.usecases.RepeatMode

data class PlayerState(
    val playingSong: Audio = Audio(),
    val isPlaying: Boolean = false,
    val currentProgress: Long = 0,
    val repeatMode: RepeatMode = RepeatMode.NO_REPEAT,
    val isShuffleEnabled: Boolean = false
)
