package com.example.musicplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Audio(
    @PrimaryKey
    val id: Long = -1,
    val uri: String = "",
    val title: String = "",
    val artistId: Long = -1,
    val artistName: String = "",
    val albumId: Long = -1,
    val albumName: String = "",
    val albumArt: String = "",
    val duration: Long = -1,
    val track: Long = -1,
    val data: String = "",
    val size: Long = 0,
    val dateModified: Long = -1,
    val dateAdded: Long = -1
)
