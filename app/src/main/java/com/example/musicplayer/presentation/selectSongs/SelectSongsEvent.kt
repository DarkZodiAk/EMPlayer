package com.example.musicplayer.presentation.selectSongs

sealed interface SelectSongsEvent {
    data class AddSong(val id: Long): SelectSongsEvent
    data class DeleteSong(val id: Long): SelectSongsEvent
    object Confirm: SelectSongsEvent
}