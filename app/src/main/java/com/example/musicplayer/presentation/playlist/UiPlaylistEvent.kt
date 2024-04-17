package com.example.musicplayer.presentation.playlist

sealed interface UiPlaylistEvent {
    object OpenPlayer: UiPlaylistEvent
}