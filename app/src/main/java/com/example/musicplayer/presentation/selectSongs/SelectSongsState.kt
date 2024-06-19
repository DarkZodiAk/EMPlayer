package com.example.musicplayer.presentation.selectSongs

import com.example.musicplayer.data.local.entity.Audio

data class SelectSongsState(
    val songs: List<Audio> = emptyList(),
    val selectedSongs: List<Long> = emptyList()
)
