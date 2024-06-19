package com.example.musicplayer.presentation.playlist

import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.Playlist

data class PlaylistState(
    val playlist: Playlist = Playlist(null, ""),
    val songs: List<Audio> = emptyList()
)
