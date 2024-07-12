package com.example.musicplayer.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    init {
        snapshotFlow { audioPlayer.playerState }.onEach { playerState ->
            state = state.copy(
                playingSong = playerState.currentAudio,
                isPlaying = playerState.isPlaying,
                currentProgress = playerState.currentTime
            )
        }.launchIn(viewModelScope)
    }

    fun onAction(action: HomeAction) {
        when(action) {
            HomeAction.OnPlayPauseClick -> {
                if(audioPlayer.playerState.isPlaying) audioPlayer.pause()
                else audioPlayer.play()
            }
            else -> Unit
        }
    }
}