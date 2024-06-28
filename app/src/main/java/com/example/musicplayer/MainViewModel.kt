package com.example.musicplayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val audioObserver: AudioObserver
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    private val channel = Channel<MainEvent>()
    val event = channel.receiveAsFlow()

    fun onAction(action: MainAction) {
        when(action) {
            is MainAction.SubmitReadPermissionInfo -> {
                if(!action.isGranted && action.shouldShowRationale) {
                    viewModelScope.launch {
                        channel.send(MainEvent.RequestReadPermission)
                    }
                } else if(!action.isGranted) {
                    state = state.copy(showReadSettingsDialog = true)
                } else {
                    state = state.copy(isLoaded = true, showReadSettingsDialog = false)
                    audioObserver.startObservingAudio()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioObserver.stopObservingAudio()
    }
}