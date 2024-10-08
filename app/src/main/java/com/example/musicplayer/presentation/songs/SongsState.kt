package com.example.musicplayer.presentation.songs

import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.SortDirection
import com.example.musicplayer.domain.SortType

data class SongsState(
    val songs: List<Song> = emptyList(),
    val playingSong: Song? = null,
    val sortType: SortType = SortType.TITLE,
    val sortDirection: SortDirection = SortDirection.ASC
)
