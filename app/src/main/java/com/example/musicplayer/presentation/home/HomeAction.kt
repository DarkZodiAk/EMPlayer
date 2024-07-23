package com.example.musicplayer.presentation.home

sealed interface HomeAction {
    object OnPlayerBarClick: HomeAction
    object OnPlayPauseClick: HomeAction
    object OnSearchButtonClick: HomeAction
}