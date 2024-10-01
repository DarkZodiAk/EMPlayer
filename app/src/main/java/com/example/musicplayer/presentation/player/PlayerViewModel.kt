package com.example.musicplayer.presentation.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioPlayer
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.domain.usecases.SwitchRepeatModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer,
    private val switchRepeatModeUseCase: SwitchRepeatModeUseCase
) : ViewModel() {

    var state by mutableStateOf(PlayerState())
        private set

    private val channel = Channel<PlayerEvent>()
    val uiEvent = channel.receiveAsFlow()

    init {
        snapshotFlow { audioPlayer.playerState }.onEach { playerState ->
            state = state.copy(
                playingSong = playerState.currentAudio ?: Audio(),
                isPlaying = playerState.isPlaying,
                currentProgress = playerState.currentTime,
                repeatMode = playerState.repeatMode,
                isShuffleEnabled = playerState.isShuffleEnabled
            )
            if(playerState.isError) channel.send(PlayerEvent.Error)
        }.launchIn(viewModelScope)

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
            PlayerAction.SwitchRepeatMode -> {
                switchRepeatModeUseCase(state.repeatMode)
            }
            PlayerAction.SwitchShuffledMode -> {
                audioPlayer.setPlaylistShuffle(!state.isShuffleEnabled)
            }
            is PlayerAction.OnSongPositionSet -> {
                audioPlayer.setPosition(action.position)
            }
            else -> Unit
        }
    }
}