package com.example.musicplayer.presentation.playlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.domain.PlayerRepository
import com.example.musicplayer.domain.songPlayer.SongPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val songPlayer: SongPlayer,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var playlistJob: Job? = null
    private var songsJob: Job? = null

    var state by mutableStateOf(PlaylistState())
        private set

    init {
        savedStateHandle.get<Long>("playlistId")?.let { id ->
            playlistJob = playerRepository.getPlaylistById(id)
                .onEach {
                    state = state.copy(
                        playlist = it ?: Playlist(null, "")
                    )
                }.launchIn(viewModelScope)
            songsJob = playerRepository.getSongsFromPlaylist(id)
                .onEach {
                    state = state.copy(
                        songs = it
                    )
                }.launchIn(viewModelScope)
        }
        SongPlayer.state.map { it.currentSong }
            .distinctUntilChanged()
            .onEach { state = state.copy(playingSong = it) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: PlaylistAction) {
        when(action) {
            PlaylistAction.OnDeletePlaylistClick -> {
                viewModelScope.launch {
                    playlistJob?.cancel()
                    songsJob?.cancel()
                    playerRepository.deletePlaylist(state.playlist)
                }
            }
            is PlaylistAction.OnRemoveSongFromPlaylistClick -> {
                viewModelScope.launch {
                    playerRepository.deleteSongFromPlaylist(state.playlist.id!!, action.songId)
                }
            }
            is PlaylistAction.OnRenamePlaylistClick -> {
                if(action.newName.isNotBlank()){
                    viewModelScope.launch {
                        playerRepository.updatePlaylist(
                            state.playlist.copy(name = action.newName)
                        )
                    }
                }
            }
            is PlaylistAction.OnSongClick -> {
                songPlayer.setPlaylist(state.songs, action.index)
            }

            else -> Unit
        }
    }
}