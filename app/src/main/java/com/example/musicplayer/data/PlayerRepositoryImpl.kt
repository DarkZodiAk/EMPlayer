package com.example.musicplayer.data

import android.content.Context
import com.example.musicplayer.data.local.dao.AudioDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.AudioPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistDao: PlaylistDao,
    private val audioDao: AudioDao
): PlayerRepository {
    override suspend fun createPlaylist(playlist: Playlist) {
        playlistDao.createPlaylist(
            playlist.copy(imageUri = emptyImageUri)
        )
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        var imageUri = playlist.imageUri

        val songs = playlistDao.getSongsFromPlaylist(playlist.id!!).first()
        if(imageUri == emptyImageUri){
            songs.forEach { song ->
                if(song.albumArt != emptyImageUri) {
                    imageUri = song.albumArt
                    return@forEach
                }
            }
        }

        playlistDao.updatePlaylist(
            playlist.copy(
                imageUri = imageUri,
                songsCount = songs.size
            )
        )
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
        val playlist = playlistDao.getPlaylistById(playlistId).first()!!
        updatePlaylist(playlist)
    }

    override suspend fun upsertAudio(audio: Audio) {
        audioDao.upsertAudio(audio)
    }

    override suspend fun deleteAudio(audio: Audio) {
        playlistDao.getPlaylistIdsWithAudio(audio.id).first().forEach { playlistId ->
            deleteAudioFromPlaylist(playlistId, audio.id)
        }
        audioDao.deleteAudio(audio)
    }

    override fun getAllAudio(): Flow<List<Audio>> {
        return audioDao.getAllAudio()
    }

    companion object {
        private var emptyImageUri: String = ""
    }

    init {
        emptyImageUri.ifEmpty { emptyImageUri = "android.resource://${context.packageName}/drawable/music_icon" }
    }
}