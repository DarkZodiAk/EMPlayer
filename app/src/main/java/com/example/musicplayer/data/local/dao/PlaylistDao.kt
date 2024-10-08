package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.SongPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist

import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

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

    @Query("SELECT * FROM song WHERE id IN (SELECT songId FROM songplaylistcross WHERE playlistId = :playlistId)")
    fun getSongsFromPlaylist(playlistId: Long): Flow<List<Song>>

    @Query("SELECT albumArt FROM song WHERE id IN (SELECT songId FROM songplaylistcross WHERE playlistId = :playlistId)")
    fun getSongAlbumArtsFromPlaylist(playlistId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(ref: SongPlaylistCross)

    @Delete
    suspend fun deleteSongFromPlaylist(ref: SongPlaylistCross)

    @Query("SELECT playlistId FROM songplaylistcross WHERE songId = :id")
    fun getPlaylistIdsWithSongs(id: Long): Flow<List<Long>>
}