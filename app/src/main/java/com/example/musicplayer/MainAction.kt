package com.example.musicplayer

sealed interface MainAction {
    data class SubmitPermissionsInfo(
        val hasRead: Boolean,
        val hasPost: Boolean,
        val shouldShowReadRationale: Boolean,
        val shouldShowPostRationale: Boolean
    ): MainAction
}