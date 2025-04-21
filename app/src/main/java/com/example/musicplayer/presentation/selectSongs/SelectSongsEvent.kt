package com.example.musicplayer.presentation.selectSongs

sealed interface SelectSongsEvent {
    object NavigateBack: SelectSongsEvent
}