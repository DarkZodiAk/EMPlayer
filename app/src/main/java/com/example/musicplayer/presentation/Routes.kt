package com.example.musicplayer.presentation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    object HomeScreen: Route
    @Serializable
    data class PlaylistScreen(val playlistId: Long): Route
    @Serializable
    data class SelectSongsScreen(val playlistId: Long): Route
    @Serializable
    object PlayerScreen: Route

}