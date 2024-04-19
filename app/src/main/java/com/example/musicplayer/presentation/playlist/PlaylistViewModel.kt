package com.example.musicplayer.presentation.playlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioPlayer
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val audioPlayer: AudioPlayer,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var playlistJob: Job? = null
    private var songsJob: Job? = null

    var playlist by mutableStateOf(Playlist(null, ""))
        private set
    var songs by mutableStateOf(listOf<Audio>())
        private set

    private val channel = Channel<UiPlaylistEvent>()
    val uiEvent = channel.receiveAsFlow()

    init {
        savedStateHandle.get<Long>("id")?.let { id ->
            playlistJob = playerRepository.getPlaylistById(id)
                .onEach {
                    playlist = it ?: Playlist(null, "")
                }.launchIn(viewModelScope)
            songsJob = playerRepository.getSongsFromPlaylist(id)
                .onEach {
                    songs = it
                }.launchIn(viewModelScope)
        }
    }

    fun onEvent(event: PlaylistEvent) {
        when(event) {
            PlaylistEvent.DeletePlaylist -> {
                viewModelScope.launch {
                    playlistJob?.cancel()
                    songsJob?.cancel()
                    playerRepository.deletePlaylist(playlist)
                }
            }
            is PlaylistEvent.RemoveSongFromPlaylist -> {
                viewModelScope.launch {
                    playerRepository.deleteAudioFromPlaylist(playlist.id!!, event.songId)
                }
            }
            is PlaylistEvent.RenamePlaylist -> {
                if(event.newName.isNotBlank()){
                    viewModelScope.launch {
                        playerRepository.upsertPlaylist(
                            playlist.copy(name = event.newName)
                        )
                    }
                }
            }
            is PlaylistEvent.PlaySong -> {
                audioPlayer.setPlaylist(songs)
                audioPlayer.setAudioIndex(event.index)
                viewModelScope.launch {
                    channel.send(UiPlaylistEvent.OpenPlayer)
                }
                //audioPlayer.play()
            }
        }
    }
}