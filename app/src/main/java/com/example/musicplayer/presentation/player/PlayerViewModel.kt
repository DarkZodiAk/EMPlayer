package com.example.musicplayer.presentation.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    var state by mutableStateOf(PlayerState())
        private set

    private val channel = Channel<PlayerEvent>()
    val uiEvent = channel.receiveAsFlow()

    init {
        audioPlayer.currentAudio.onEach { state = state.copy(playingSong = it) }.launchIn(viewModelScope)
        audioPlayer.isPlayingFlow.onEach { state = state.copy(isPlaying = it) }.launchIn(viewModelScope)
        audioPlayer.currentTime.onEach { state = state.copy(currentTime = it) }.launchIn(viewModelScope)
        audioPlayer.isError
            .onEach { if(it) channel.send(PlayerEvent.Error) }
            .launchIn(viewModelScope)
        audioPlayer.play()
    }

    fun onAction(action: PlayerAction) {
        when(action) {
            PlayerAction.OnNextSongClick -> {
                audioPlayer.next()
            }
            PlayerAction.OnPrevSongClick -> {
                audioPlayer.previous()
            }
            PlayerAction.OnPlayPauseClick -> {
                if(state.isPlaying) audioPlayer.pause()
                else audioPlayer.play()
            }
            PlayerAction.OnBackClick -> {
                audioPlayer.stop()
            }
        }
    }
}