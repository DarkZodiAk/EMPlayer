package com.example.musicplayer.presentation.songs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.songPlayer.SongPlayer
import com.example.musicplayer.domain.usecases.GetSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    private val songPlayer: SongPlayer
) : ViewModel() {

    var state by mutableStateOf(SongsState())
        private set

    private var getSongsJob: Job? = null

    init {
        getAllSongs()

        SongPlayer.state.map { it.currentSong }
            .distinctUntilChanged()
            .onEach { state = state.copy(playingSong = it) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SongsAction) {
        when(action) {
            is SongsAction.PlaySong -> {
                songPlayer.setPlaylist(state.songs, action.index)
            }

            is SongsAction.SwitchSortType -> {
                state = state.copy(
                    sortType = action.sortType,
                    sortDirection = if(action.sortType == state.sortType) state.sortDirection.switch()
                                    else state.sortDirection
                )
                getAllSongs()
            }
        }
    }

    private fun getAllSongs() {
        getSongsJob?.cancel()
        getSongsJob = getSongsUseCase(state.sortType, state.sortDirection)
            .onEach { state = state.copy(songs = it) }
            .launchIn(viewModelScope)
    }
}