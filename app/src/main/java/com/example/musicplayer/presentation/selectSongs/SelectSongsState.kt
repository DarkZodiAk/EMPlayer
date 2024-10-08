package com.example.musicplayer.presentation.selectSongs

import com.example.musicplayer.data.local.entity.Song

data class SelectSongsState(
    val songs: List<Song> = emptyList(),
    val selectedSongs: List<Long> = emptyList()
)
