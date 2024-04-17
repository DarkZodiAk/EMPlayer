package com.example.musicplayer.presentation.playlists

sealed interface PlaylistsEvent {
    data class NewPlaylist(val name: String): PlaylistsEvent
}