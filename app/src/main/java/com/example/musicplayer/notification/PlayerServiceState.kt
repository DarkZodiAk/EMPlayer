package com.example.musicplayer.notification

import com.example.musicplayer.data.local.entity.Song

data class PlayerServiceState(
    val currentSong: Song = Song(),
    val isPlaying: Boolean = false,
)