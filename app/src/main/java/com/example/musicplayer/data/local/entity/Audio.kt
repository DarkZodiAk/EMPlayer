package com.example.musicplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Audio(
    @PrimaryKey
    val id: Long = -1,
    val title: String = "",
    val artist: String = "",
    val duration: Long = 0,
    val uri: String = "",
    val dateModified: Long = 0
)
