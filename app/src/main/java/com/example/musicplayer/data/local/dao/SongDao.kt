package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.SongInCurrentPlaylist
import com.example.musicplayer.data.local.entity.SongInInitialPlaylist
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


    @Insert
    suspend fun addSongToInitialPlaylist(song: SongInInitialPlaylist)

    @Insert
    suspend fun addSongToCurrentPlaylist(song: SongInCurrentPlaylist)

    @Query("DELETE FROM songininitialplaylist")
    suspend fun deleteSongsFromInitialPlaylist()

    @Query("DELETE FROM songincurrentplaylist")
    suspend fun deleteSongsFromCurrentPlaylist()

    @Query("SELECT * FROM songininitialplaylist")
    suspend fun getSongsFromInitialPlaylist(): List<SongInInitialPlaylist>

    @Query("SELECT * FROM songincurrentplaylist")
    suspend fun getSongsFromCurrentPlaylist(): List<SongInCurrentPlaylist>
}