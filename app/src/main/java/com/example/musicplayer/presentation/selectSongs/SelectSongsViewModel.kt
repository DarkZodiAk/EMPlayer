package com.example.musicplayer.presentation.selectSongs

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectSongsViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val songs = playerRepository.getAllAudio()
        .map { it.sortedBy { it.dateModified }.reversed() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private var playlistId = -1L

    private val selectedSongIds = mutableStateListOf<Long>()
    val selectedSongs: List<Long> = selectedSongIds


    init {
        savedStateHandle.get<Long>("playlistId")?.let { id ->
            playlistId = id
        }
    }


    fun onEvent(event: SelectSongsEvent) {
        when(event) {
            is SelectSongsEvent.AddSong -> {
                selectedSongIds.add(event.id)
            }
            is SelectSongsEvent.DeleteSong -> {
                selectedSongIds.remove(event.id)
            }
            SelectSongsEvent.Confirm -> {
                selectedSongIds.forEach { songId ->
                    viewModelScope.launch {
                        playerRepository.addAudioToPlaylist(playlistId, songId)
                    }
                }
            }
        }
    }
}