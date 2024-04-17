package com.example.musicplayer.presentation.player

sealed interface PlayerEvent {
    object PlayPauseClicked: PlayerEvent
    object NextSong: PlayerEvent
    object PrevSong: PlayerEvent
    object ToPreviousScreen: PlayerEvent
}