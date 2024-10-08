package com.example.musicplayer.data

import android.content.Context
import com.example.musicplayer.data.local.dao.SongDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.SongPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao
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

    override fun getSongsFromPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsFromPlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(SongPlaylistCross(songId, playlistId))
    }

    override suspend fun deleteSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.deleteSongFromPlaylist(SongPlaylistCross(songId, playlistId))
        val playlist = playlistDao.getPlaylistById(playlistId).first()!!
        updatePlaylist(playlist)
    }

    override suspend fun upsertSong(song: Song) {
        songDao.upsertSong(song)
    }

    override suspend fun deleteSong(song: Song) {
        playlistDao.getPlaylistIdsWithSongs(song.id).first().forEach { playlistId ->
            deleteSongFromPlaylist(playlistId, song.id)
        }
        songDao.deleteSong(song)
    }

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs()
    }

    companion object {
        private var emptyImageUri: String = ""
    }

    init {
        emptyImageUri.ifEmpty { emptyImageUri = "android.resource://${context.packageName}/drawable/music_icon" }
    }
}