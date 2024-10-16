package com.example.musicplayer.domain.songPlayer

import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.usecases.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface SongPlayer {
    fun play()
    fun pause()
    fun stop()
    fun next()
    fun previous()
    fun setPlaylist(songs: List<Song>, index: Int)
    fun setPosition(position: Long)
    fun setRepeatMode(repeatMode: RepeatMode)
    fun setShuffleMode(enabled: Boolean)


    companion object {
        private val _state = MutableStateFlow(SongPlayerState())
        val state = _state.asStateFlow()

        fun updateState(state: SongPlayerState) {
            _state.update { state }
        }
    }
}