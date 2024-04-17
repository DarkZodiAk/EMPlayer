package com.example.musicplayer.presentation

sealed class Screen(val route: String) {
    object SongsScreen: Screen("songs")
    object PlaylistsScreen: Screen("playlists")
    object PlaylistScreen: Screen("playlist")
    object SelectSongsScreen: Screen("select_songs")
    object PlayerScreen: Screen("player")
}