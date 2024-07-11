package com.example.musicplayer.presentation.playlists

sealed interface PlaylistsAction {
    data class OnCreatePlaylistClick(val name: String): PlaylistsAction
    data class OnPlaylistClick(val id: Long): PlaylistsAction
}