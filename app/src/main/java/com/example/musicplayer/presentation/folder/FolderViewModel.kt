package com.example.musicplayer.presentation.folder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.local.entity.Folder
import com.example.musicplayer.domain.PlayerRepository
import com.example.musicplayer.domain.songPlayer.SongPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val repository: PlayerRepository,
    private val songPlayer: SongPlayer,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var state by mutableStateOf(FolderState())
        private set

    //TODO(Добавить обработку ошибки удаления папки, которая открыта)

    init {
        savedStateHandle.get<Long>("folderId")?.let { id ->
            repository.getFolderById(id).onEach { folder ->
                state = state.copy(folder = folder ?: Folder())
            }.launchIn(viewModelScope)
            repository.getSongsFromFolder(id).onEach { songs ->
                state = state.copy(songs = songs)
            }.launchIn(viewModelScope)
        }

        SongPlayer.state.map { it.currentSong }
            .distinctUntilChanged()
            .onEach { state = state.copy(playingSong = it) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: FolderAction) {
        when(action) {
            is FolderAction.OnSongClick -> {
                songPlayer.setPlaylist(state.songs, action.index)
                songPlayer.play()
            }
            else -> Unit
        }
    }
}