package com.example.musicplayer.presentation.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioPlayer
import com.example.musicplayer.data.local.entity.Audio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    var song by mutableStateOf(Audio(0L, "", "", 0L, "", 0L))
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var currentTime by mutableLongStateOf(0L)
        private set

    private val channel = Channel<UiPlayerEvent>()
    val uiEvent = channel.receiveAsFlow()

    init {
        audioPlayer.currentAudio.onEach { song = it }.launchIn(viewModelScope)
        audioPlayer.isPlayingFlow.onEach { isPlaying = it }.launchIn(viewModelScope)
        audioPlayer.currentTime.onEach { currentTime = it }.launchIn(viewModelScope)
        audioPlayer.play()
    }

    fun onEvent(event: PlayerEvent) {
        when(event) {
            PlayerEvent.NextSong -> {
                audioPlayer.next()
            }
            PlayerEvent.PrevSong -> {
                audioPlayer.previous()
            }
            PlayerEvent.PlayPauseClicked -> {
                if(isPlaying) audioPlayer.pause()
                else audioPlayer.play()
            }
            PlayerEvent.ToPreviousScreen -> {
                audioPlayer.stop()
                viewModelScope.launch {
                    channel.send(UiPlayerEvent.Back)
                }
            }
        }
    }
}