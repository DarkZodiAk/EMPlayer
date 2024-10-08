package com.example.musicplayer.presentation.playlist

import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.Playlist

data class PlaylistState(
    val playlist: Playlist = Playlist(null, ""),
    val songs: List<Song> = emptyList(),
    val playingSong: Song? = null
)
