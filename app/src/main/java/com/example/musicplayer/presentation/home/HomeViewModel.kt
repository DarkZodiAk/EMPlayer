package com.example.musicplayer.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.songPlayer.SongPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songPlayer: SongPlayer
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    init {
        SongPlayer.state.onEach { playerState ->
            state = state.copy(
                playingSong = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                currentProgress = playerState.currentPosition
            )
        }.launchIn(viewModelScope)
    }

    fun onAction(action: HomeAction) {
        when(action) {
            HomeAction.OnPlayPauseClick -> {
                if(state.isPlaying) songPlayer.pause()
                else songPlayer.play()
            }
            else -> Unit
        }
    }
}