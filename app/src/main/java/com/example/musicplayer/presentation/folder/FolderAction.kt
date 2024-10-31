package com.example.musicplayer.presentation.folder

sealed interface FolderAction {
    object OnBack: FolderAction
    data class OnSongClick(val index: Int): FolderAction
}