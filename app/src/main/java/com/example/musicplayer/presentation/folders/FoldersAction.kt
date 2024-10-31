package com.example.musicplayer.presentation.folders

sealed interface FoldersAction {
    data class OnFolderClick(val id: Long): FoldersAction
}