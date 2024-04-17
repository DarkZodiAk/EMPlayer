package com.example.musicplayer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["audioId", "playlistId"],
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Playlist::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("playlistId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Audio::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("audioId"),
            onDelete = ForeignKey.CASCADE
        )
    )
)
data class AudioPlaylistCross(
    val audioId: Long,
    val playlistId: Long
)
