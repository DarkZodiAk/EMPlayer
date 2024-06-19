package com.example.musicplayer.presentation.player

sealed interface PlayerAction {
    object OnPlayPauseClick: PlayerAction
    object OnNextSongClick: PlayerAction
    object OnPrevSongClick: PlayerAction
    object OnBackClick: PlayerAction
}