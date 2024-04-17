package com.example.musicplayer.presentation.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioPlayer
import com.example.musicplayer.data.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    var songs = playerRepository.getAllAudio()
        .map { it.sortedBy { it.dateModified }.reversed() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val channel = Channel<UiSongsEvent>()
    val uiEvent = channel.receiveAsFlow()

    fun onEvent(event: SongsEvent) {
        when(event) {
            is SongsEvent.PlaySong -> {
                audioPlayer.setPlaylist(songs.value)
                audioPlayer.setAudioIndex(event.index)
                viewModelScope.launch {
                    channel.send(UiSongsEvent.OpenPlayer)
                }
                //audioPlayer.play()
            }
        }
    }
}