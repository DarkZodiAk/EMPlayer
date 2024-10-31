package com.example.musicplayer.presentation.folder

import com.example.musicplayer.data.local.entity.Folder
import com.example.musicplayer.data.local.entity.Song

data class FolderState(
    val folder: Folder = Folder(),
    val songs: List<Song> = emptyList(),
    val playingSong: Song? = null
)
