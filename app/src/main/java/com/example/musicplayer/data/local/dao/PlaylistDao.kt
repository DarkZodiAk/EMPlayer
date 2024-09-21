package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.AudioPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist

import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Transaction
    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Insert
    suspend fun createPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlist")
    fun getPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist WHERE id = :id")
    fun getPlaylistById(id: Long): Flow<Playlist?>

    @Query("SELECT * FROM audio WHERE id IN (SELECT audioId FROM audioplaylistcross WHERE playlistId = :playlistId)")
    fun getSongsFromPlaylist(playlistId: Long): Flow<List<Audio>>

    @Query("SELECT albumArt FROM audio WHERE id IN (SELECT audioId FROM audioplaylistcross WHERE playlistId = :playlistId)")
    fun getSongAlbumArtsFromPlaylist(playlistId: Long): Flow<List<String>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAudioToPlaylist(ref: AudioPlaylistCross)

    @Transaction
    @Delete
    suspend fun deleteAudioFromPlaylist(ref: AudioPlaylistCross)

    @Query("SELECT playlistId FROM audioplaylistcross WHERE audioId = :id")
    fun getPlaylistIdsWithAudio(id: Long): Flow<List<Long>>
}