package com.example.musicplayer.presentation.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.PlayerRepository
import com.example.musicplayer.data.local.entity.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    val playlists = playerRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun onEvent(event: PlaylistsEvent) {
        when(event) {
            is PlaylistsEvent.NewPlaylist -> {
                if(event.name.isNotBlank()) {
                    viewModelScope.launch {
                        playerRepository.upsertPlaylist(Playlist(null, event.name))
                    }
                }
            }
        }
    }
}