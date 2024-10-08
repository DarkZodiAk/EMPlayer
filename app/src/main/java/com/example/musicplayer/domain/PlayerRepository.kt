package com.example.musicplayer.domain

import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.Playlist
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    suspend fun createPlaylist(playlist: Playlist)

    suspend fun updatePlaylist(playlist: Playlist)

    suspend fun deletePlaylist(playlist: Playlist)

    fun getPlaylistById(id: Long): Flow<Playlist?>

    fun getPlaylists(): Flow<List<Playlist>>

    fun getSongsFromPlaylist(playlistId: Long): Flow<List<Song>>

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)

    suspend fun deleteSongFromPlaylist(playlistId: Long, songId: Long)

    suspend fun upsertSong(song: Song)

    suspend fun deleteSong(song: Song)

    fun getAllSongs(): Flow<List<Song>>
}