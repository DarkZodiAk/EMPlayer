package com.example.musicplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String = "",
    val songsCount: Int = 0,
    val imageUri: String = ""
)
