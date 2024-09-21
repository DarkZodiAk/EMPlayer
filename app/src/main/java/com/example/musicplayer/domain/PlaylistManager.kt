@file:Suppress("OPT_IN_USAGE")

package com.example.musicplayer.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
* This class is used for adding songs to playlist
* beyond viewModelScope
* */
class PlaylistManager @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val channel = Channel<Pair<SongId, PlaylistId>>(capacity = Channel.UNLIMITED)
    private val flowChannel = channel.consumeAsFlow()


    init {
        flowChannel
            .onEach {
                playerRepository.addAudioToPlaylist(it.second, it.first)
            }.debounce(100L)
            .onEach {
                val playlist = playerRepository.getPlaylistById(it.second).first()!!
                playerRepository.updatePlaylist(playlist)
            }.launchIn(scope)
    }

    fun addSong(songId: Long, playlistId: Long) {
        scope.launch {
            channel.send(Pair(songId, playlistId))
        }
    }
}

typealias PlaylistId = Long
typealias SongId = Long