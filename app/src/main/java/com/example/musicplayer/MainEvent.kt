package com.example.musicplayer

sealed interface MainEvent {
    object RequestReadPermission: MainEvent
}