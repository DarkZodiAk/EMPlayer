package com.example.musicplayer

sealed interface MainAction {
    data class SubmitReadPermissionInfo(val isGranted: Boolean, val shouldShowRationale: Boolean): MainAction
}