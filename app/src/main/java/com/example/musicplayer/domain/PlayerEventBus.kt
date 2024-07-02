package com.example.musicplayer.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PlayerEventBus {
    private val _playerBus = MutableSharedFlow<AudioPlayerState>(extraBufferCapacity = 1, replay = 1)
    val playerBus = _playerBus.asSharedFlow()

    suspend fun sendState(state: AudioPlayerState) {
        _playerBus.emit(state)
    }
}