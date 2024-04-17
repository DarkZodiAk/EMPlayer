package com.example.musicplayer.presentation.playlist

sealed interface PlaylistEvent {
    data class RemoveSongFromPlaylist(val songId: Long): PlaylistEvent
    data class RenamePlaylist(val newName: String): PlaylistEvent
    object DeletePlaylist: PlaylistEvent
    data class PlaySong(val index: Int): PlaylistEvent
}