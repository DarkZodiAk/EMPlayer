package com.example.musicplayer.presentation.songs

sealed interface UiSongsEvent {
    object OpenPlayer: UiSongsEvent
}