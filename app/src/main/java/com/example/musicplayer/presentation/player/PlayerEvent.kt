package com.example.musicplayer.presentation.player

sealed interface PlayerEvent {
    object Error: PlayerEvent
}