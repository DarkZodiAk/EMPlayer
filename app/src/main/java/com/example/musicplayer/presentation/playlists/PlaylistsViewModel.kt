package com.example.musicplayer.presentation.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.domain.PlayerRepository
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

    fun onAction(action: PlaylistsAction) {
        when(action) {
            is PlaylistsAction.OnCreatePlaylistClick -> {
                if(action.name.isNotBlank()) {
                    viewModelScope.launch {
                        playerRepository.createPlaylist(Playlist(null, action.name))
                    }
                }
            }
            else -> Unit
        }
    }
}