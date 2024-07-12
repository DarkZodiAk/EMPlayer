package com.example.musicplayer.presentation.songs

import com.example.musicplayer.data.local.entity.Audio

data class SongsState(
    val songs: List<Audio> = emptyList(),
    val playingSong: Audio? = null
)
