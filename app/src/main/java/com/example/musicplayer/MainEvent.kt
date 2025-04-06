package com.example.musicplayer

sealed interface MainEvent {
    object RequestPermissions: MainEvent
}