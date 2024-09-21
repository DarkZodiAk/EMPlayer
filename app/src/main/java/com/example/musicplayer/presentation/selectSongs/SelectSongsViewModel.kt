package com.example.musicplayer.presentation.selectSongs

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectSongsViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(SelectSongsState())
        private set

    private var playlistId = -1L

    init {
        playerRepository.getAllAudio()
            .map { it.sortedBy { it.title } }
            .onEach { state = state.copy(songs = it) }
            .launchIn(viewModelScope)
        savedStateHandle.get<Long>("playlistId")?.let { id ->
            playlistId = id
        }
    }


    fun onAction(action: SelectSongsAction) {
        when(action) {
            is SelectSongsAction.AddSong -> {
                updateSelectedSongs(state.selectedSongs.toMutableList() + action.id)
            }
            is SelectSongsAction.DeleteSong -> {
                updateSelectedSongs(state.selectedSongs.toMutableList() - action.id)
            }
            SelectSongsAction.OnConfirmClick -> {
                state.selectedSongs.forEach { songId ->
                    viewModelScope.launch {
                        playerRepository.addAudioToPlaylist(playlistId, songId)
                    }
                }
            }
            else -> Unit
        }
    }

    private fun updateSelectedSongs(selectedSongs: List<Long>) {
        state = state.copy(selectedSongs = selectedSongs)
    }

    override fun onCleared() {
        Log.d("PLAYLIST ADD", "VIEWMODEL CLEARED")
        super.onCleared()
    }
}