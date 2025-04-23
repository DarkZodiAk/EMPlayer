package com.example.musicplayer.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/*
* This class is used for adding songs to playlist
* beyond viewModelScope
* */
@Singleton
class PlaylistManager @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun addSongs(songIds: List<Long>, playlistId: Long) {
        val copiedSongIds = songIds.toList()
        scope.launch {
            copiedSongIds.forEach { songId ->
                playerRepository.addSongToPlaylist(playlistId, songId)
            }
            val playlist = playerRepository.getPlaylistById(playlistId).first()!!
            playerRepository.updatePlaylist(playlist)
        }
    }
}