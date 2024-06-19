package com.example.musicplayer.presentation.playlist

sealed interface PlaylistAction {
    data class OnRemoveSongFromPlaylistClick(val songId: Long): PlaylistAction
    data class OnRenamePlaylistClick(val newName: String): PlaylistAction
    object OnDeletePlaylistClick: PlaylistAction
    data class OnSongClick(val index: Int): PlaylistAction
    object OnAddSongsClick: PlaylistAction
    object OnBackClick: PlaylistAction
}