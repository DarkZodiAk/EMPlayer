package com.example.musicplayer.presentation.songs

import com.example.musicplayer.domain.SortType


sealed interface SongsAction {
    data class PlaySong(val index: Int): SongsAction
    data class SwitchSortType(val sortType: SortType): SongsAction
}