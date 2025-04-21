package com.example.musicplayer.presentation.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.songPlayer.SongPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

        viewModelScope.launch {
            while(true) {
                Log.d(this@HomeViewModel.javaClass.simpleName, "Hello")
                delay(2000L)
            }
        }
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