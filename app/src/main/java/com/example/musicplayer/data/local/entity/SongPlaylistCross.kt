package com.example.musicplayer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["songId", "playlistId"],
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Playlist::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("playlistId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("songId"),
            onDelete = ForeignKey.CASCADE
        )
    )
)
data class SongPlaylistCross(
    val songId: Long,
    val playlistId: Long
)
