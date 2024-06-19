package com.example.musicplayer.presentation.selectSongs

sealed interface SelectSongsAction {
    data class AddSong(val id: Long): SelectSongsAction
    data class DeleteSong(val id: Long): SelectSongsAction
    object OnConfirmClick: SelectSongsAction
    object OnBackClick: SelectSongsAction
}