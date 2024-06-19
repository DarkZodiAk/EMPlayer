package com.example.musicplayer.presentation.songs


sealed interface SongsAction {
    data class PlaySong(val index: Int): SongsAction
    object OnPlaylistsClick: SongsAction
}