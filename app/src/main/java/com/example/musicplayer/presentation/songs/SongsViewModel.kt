package com.example.musicplayer.presentation.songs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioPlayer
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    var state by mutableStateOf(SongsState())
        private set

    init {
        playerRepository.getAllAudio()
            .map { it.sortedBy { it.title } }
            .onEach { state = state.copy(songs = it) }
            .launchIn(viewModelScope)

        snapshotFlow { audioPlayer.playerState.currentAudio }
            .onEach { state = state.copy(playingSong = it) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SongsAction) {
        when(action) {
            is SongsAction.PlaySong -> {
                audioPlayer.setPlaylist(state.songs)
                audioPlayer.setAudioIndex(action.index)
                //audioPlayer.play()
            }
        }
    }
}