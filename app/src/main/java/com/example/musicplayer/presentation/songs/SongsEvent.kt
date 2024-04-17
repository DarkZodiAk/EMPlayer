package com.example.musicplayer.presentation.songs


sealed interface SongsEvent {
    data class PlaySong(val index: Int): SongsEvent
}