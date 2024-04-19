package com.example.musicplayer.domain

import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.Playlist
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    suspend fun upsertPlaylist(playlist: Playlist)

    suspend fun deletePlaylist(playlist: Playlist)

    fun getPlaylistById(id: Long): Flow<Playlist?>

    fun getPlaylists(): Flow<List<Playlist>>

    fun getSongsFromPlaylist(playlistId: Long): Flow<List<Audio>>

    suspend fun addAudioToPlaylist(playlistId: Long, audioId: Long)

    suspend fun deleteAudioFromPlaylist(playlistId: Long, audioId: Long)

    suspend fun upsertAudio(audio: Audio)

    suspend fun deleteAudio(audio: Audio)

    fun getAllAudio(): Flow<List<Audio>>
}