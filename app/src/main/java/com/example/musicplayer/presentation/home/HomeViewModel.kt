package com.example.musicplayer.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.SongPlayer
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
        snapshotFlow { songPlayer.playerState }.onEach { playerState ->
            state = state.copy(
                playingSong = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                currentProgress = playerState.currentTime
            )
        }.launchIn(viewModelScope)
    }

    fun onAction(action: HomeAction) {
        when(action) {
            HomeAction.OnPlayPauseClick -> {
                if(songPlayer.playerState.isPlaying) songPlayer.pause()
                else songPlayer.play()
            }
            else -> Unit
        }
    }
}