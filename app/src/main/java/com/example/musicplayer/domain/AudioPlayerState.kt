package com.example.musicplayer.domain

import com.example.musicplayer.data.local.entity.Audio

data class AudioPlayerState(
    val currentAudio: Audio? = null,
    val isPlaying: Boolean = false,
    val currentTime: Long = 0,
    val isError: Boolean = false
)
