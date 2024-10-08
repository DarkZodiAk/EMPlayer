package com.example.musicplayer.data

import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.domain.usecases.RepeatMode

data class AudioPlayerState(
    val currentAudio: Audio? = null,
    val isPlaying: Boolean = false,
    val currentTime: Long = 0L,
    val isError: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NO_REPEAT,
    val isShuffleEnabled: Boolean = false
)
