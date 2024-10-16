@file:Suppress("OPT_IN_USAGE")

package com.example.musicplayer.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.songPlayer.SongPlayer
import com.example.musicplayer.domain.usecases.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase,
    private val songPlayer: SongPlayer
) : ViewModel() {

    private var searchSongsJob: Job? = null

    var state by mutableStateOf(SearchState())
        private set

    init {
        searchSongs("")
        snapshotFlow { state.searchQuery }
            .debounce(200L)
            .onEach { searchSongs(it) }
            .launchIn(viewModelScope)

        SongPlayer.state.map { it.currentSong }
            .distinctUntilChanged()
            .onEach { state = state.copy(playingSong = it) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SearchAction) {
        when(action) {
            is SearchAction.ModifySearchQuery -> {
                state = state.copy(searchQuery = action.query)
            }
            is SearchAction.PlaySong -> {
                songPlayer.setPlaylist(state.songs, action.index)
            }
            else -> Unit
        }
    }

    private fun searchSongs(searchQuery: String) {
        searchSongsJob?.cancel()
        searchSongsJob = searchSongsUseCase(searchQuery)
            .onEach { state = state.copy(songs = it) }
            .launchIn(viewModelScope)
    }
}