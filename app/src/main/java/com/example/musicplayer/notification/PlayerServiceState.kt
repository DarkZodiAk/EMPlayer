package com.example.musicplayer.notification

import com.example.musicplayer.data.local.entity.Audio

data class PlayerServiceState(
    val currentAudio: Audio = Audio(),
    val isPlaying: Boolean = false,
)