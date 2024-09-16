package com.example.musicplayer.presentation.search

sealed interface SearchAction {
    data class PlaySong(val index: Int): SearchAction
    data class ModifySearchQuery(val query: String): SearchAction
    object OnBackClick: SearchAction
}