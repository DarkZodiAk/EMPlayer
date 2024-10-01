package com.example.musicplayer.presentation.player

sealed interface PlayerAction {
    object OnPlayPauseClick: PlayerAction
    object OnNextSongClick: PlayerAction
    object OnPrevSongClick: PlayerAction
    object OnBackClick: PlayerAction
    object SwitchRepeatMode: PlayerAction
    object SwitchShuffledMode: PlayerAction
    data class OnSongPositionSet(val position: Long): PlayerAction
}