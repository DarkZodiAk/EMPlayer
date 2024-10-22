package com.example.musicplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String = "",
    val absoluteName: String = ""
)