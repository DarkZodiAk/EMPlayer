package com.example.musicplayer.presentation.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioPlayer
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    var songs = playerRepository.getAllAudio()
        .map { it.sortedBy { it.title } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun onAction(action: SongsAction) {
        when(action) {
            is SongsAction.PlaySong -> {
                audioPlayer.setPlaylist(songs.value)
                audioPlayer.setAudioIndex(action.index)
                //audioPlayer.play()
            }
            else -> Unit
        }
    }
}