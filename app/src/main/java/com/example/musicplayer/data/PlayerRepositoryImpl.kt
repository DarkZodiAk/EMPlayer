package com.example.musicplayer.data

import android.content.Context
import android.util.Log
import com.example.musicplayer.data.local.dao.AudioDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.AudioPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
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
        playlistDao.updatePlaylist(playlist)
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
        Log.d("PLAYLIST ADD START", "Add audio $audioId to $playlistId")
        playlistDao.addAudioToPlaylist(AudioPlaylistCross(audioId, playlistId))
        Log.d("PLAYLIST ADD TO CROSS", "Add audio $audioId to $playlistId")
        val playlist = playlistDao.getPlaylistById(playlistId).first()!!
        val audioAlbumArt = audioDao.getAudioAlbumArtById(audioId).first()!!

        var newImageUri = emptyImageUri
        if(playlist.imageUri == emptyImageUri && audioAlbumArt != emptyImageUri) {
            newImageUri = audioAlbumArt
        }

        Log.d("PLAYLIST ADD BEFORE UPDATE", "Add audio $audioId to $playlistId")
        playlistDao.updatePlaylist(
            playlist.copy(
                imageUri = newImageUri,
                songsCount = playlist.songsCount + 1
            )
        )
        Log.d("PLAYLIST ADD FINISHED", "Add audio $audioId to $playlistId")
    }

    override suspend fun deleteAudioFromPlaylist(playlistId: Long, audioId: Long) {
        playlistDao.deleteAudioFromPlaylist(AudioPlaylistCross(audioId, playlistId))
        val playlist = playlistDao.getPlaylistById(playlistId).first()!!
        val audioAlbumArt = audioDao.getAudioAlbumArtById(audioId).first()!!

        var newImageUri = emptyImageUri
        if(playlist.imageUri == audioAlbumArt) {
            playlistDao.getSongAlbumArtsFromPlaylist(playlistId).first().forEach { albumArt ->
                if(albumArt != emptyImageUri) {
                    newImageUri = albumArt
                }
            }
        }

        playlistDao.updatePlaylist(
            playlist.copy(
                imageUri = newImageUri,
                songsCount = playlist.songsCount - 1
            )
        )
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