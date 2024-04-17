package com.example.musicplayer.data

import com.example.musicplayer.data.local.dao.AudioDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.AudioPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist
import kotlinx.coroutines.flow.Flow

class PlayerRepository(
    private val playlistDao: PlaylistDao,
    private val audioDao: AudioDao
) {
    suspend fun upsertPlaylist(playlist: Playlist) {
        playlistDao.upsertPlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    fun getPlaylistById(id: Long): Flow<Playlist> {
        return playlistDao.getPlaylistById(id)
    }

    fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylists()
    }

    fun getSongsFromPlaylist(playlistId: Long): Flow<List<Audio>> {
        return playlistDao.getSongsFromPlaylist(playlistId)
    }

    suspend fun addAudioToPlaylist(playlistId: Long, audioId: Long) {
        playlistDao.addAudioToPlaylist(AudioPlaylistCross(audioId, playlistId))
    }

    suspend fun deleteAudioFromPlaylist(playlistId: Long, audioId: Long) {
        playlistDao.deleteAudioFromPlaylist(AudioPlaylistCross(audioId, playlistId))
    }

    //Махинации с аудио
    suspend fun upsertAudio(audio: Audio) {
        audioDao.upsertAudio(audio)
    }

    suspend fun deleteAudio(audio: Audio) {
        audioDao.deleteAudio(audio)
    }

    fun getAllAudio(): Flow<List<Audio>> {
        return audioDao.getAllAudio()
    }
}