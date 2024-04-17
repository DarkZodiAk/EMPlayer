package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.AudioPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist

import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Upsert
    suspend fun upsertPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlist")
    fun getPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist WHERE id = :id")
    fun getPlaylistById(id: Long): Flow<Playlist>

    @Query("SELECT * FROM audio WHERE id IN (SELECT audioId FROM audioplaylistcross WHERE playlistId = :playlistId)")
    fun getSongsFromPlaylist(playlistId: Long): Flow<List<Audio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAudioToPlaylist(ref: AudioPlaylistCross)

    @Delete
    suspend fun deleteAudioFromPlaylist(ref: AudioPlaylistCross)
}