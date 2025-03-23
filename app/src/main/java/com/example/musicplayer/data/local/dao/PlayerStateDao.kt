package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.data.local.entity.SongInCurrentPlaylist
import com.example.musicplayer.data.local.entity.SongInInitialPlaylist

@Dao
interface PlayerStateDao {
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