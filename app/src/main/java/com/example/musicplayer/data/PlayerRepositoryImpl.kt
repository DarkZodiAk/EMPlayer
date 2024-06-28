package com.example.musicplayer.data

import com.example.musicplayer.data.local.dao.AudioDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.AudioPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.domain.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val audioDao: AudioDao
): PlayerRepository {
    override suspend fun upsertPlaylist(playlist: Playlist) {
        playlistDao.upsertPlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    override fun getPlaylistById(id: Long): Flow<Playlist?> {
        return playlistDao.getPlaylistById(id)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylists()
    }

    override fun getSongsFromPlaylist(playlistId: Long): Flow<List<Audio>> {
        return playlistDao.getSongsFromPlaylist(playlistId)
    }

    override suspend fun addAudioToPlaylist(playlistId: Long, audioId: Long) {
        playlistDao.addAudioToPlaylist(AudioPlaylistCross(audioId, playlistId))
    }

    override suspend fun deleteAudioFromPlaylist(playlistId: Long, audioId: Long) {
        playlistDao.deleteAudioFromPlaylist(AudioPlaylistCross(audioId, playlistId))
    }

    //Махинации с аудио
    override suspend fun upsertAudio(audio: Audio) {
        audioDao.upsertAudio(audio)
    }

    override suspend fun deleteAudio(audio: Audio) {
        audioDao.deleteAudio(audio)
    }

    override fun getAllAudio(): Flow<List<Audio>> {
        return audioDao.getAllAudio()
    }
}