package com.example.musicplayer

sealed interface UiMainEvent {
    data class AskForPermission(val permission: String): UiMainEvent
}