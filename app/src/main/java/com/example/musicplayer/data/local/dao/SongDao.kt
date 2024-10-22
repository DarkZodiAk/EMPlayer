package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.musicplayer.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Upsert
    suspend fun upsertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("SELECT * FROM song")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM song WHERE id = :id")
    suspend fun getSongById(id: Long): Song?
}