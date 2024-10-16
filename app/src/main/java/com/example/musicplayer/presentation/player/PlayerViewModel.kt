package com.example.musicplayer.presentation.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.songPlayer.SongPlayer
import com.example.musicplayer.domain.usecases.SwitchRepeatModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val songPlayer: SongPlayer,
    private val switchRepeatModeUseCase: SwitchRepeatModeUseCase
) : ViewModel() {

    var state by mutableStateOf(PlayerState())
        private set

    private val channel = Channel<PlayerEvent>()
    val uiEvent = channel.receiveAsFlow()

    init {
        SongPlayer.state.onEach { playerState ->
            state = state.copy(
                playingSong = playerState.currentSong ?: Song(),
                isPlaying = playerState.isPlaying,
                currentProgress = playerState.currentTime,
                repeatMode = playerState.repeatMode,
                isShuffleEnabled = playerState.isShuffleEnabled
            )
            if(playerState.isError) channel.send(PlayerEvent.Error)
        }.launchIn(viewModelScope)

        songPlayer.play()
    }

    fun onAction(action: PlayerAction) {
        when(action) {
            PlayerAction.OnNextSongClick -> {
                songPlayer.next()
            }
            PlayerAction.OnPrevSongClick -> {
                songPlayer.previous()
            }
            PlayerAction.OnPlayPauseClick -> {
                if(state.isPlaying) songPlayer.pause()
                else songPlayer.play()
            }
            PlayerAction.SwitchRepeatMode -> {
                switchRepeatModeUseCase(state.repeatMode)
            }
            PlayerAction.SwitchShuffledMode -> {
                songPlayer.setShuffleMode(!state.isShuffleEnabled)
            }
            is PlayerAction.OnSongPositionSet -> {
                songPlayer.setPosition(action.position)
            }
            else -> Unit
        }
    }
}