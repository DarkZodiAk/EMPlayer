package com.example.musicplayer.domain.songPlayer

import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.usecases.RepeatMode

data class SongPlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentTime: Long = 0L,
    val isError: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NO_REPEAT,
    val isShuffleEnabled: Boolean = false
)
