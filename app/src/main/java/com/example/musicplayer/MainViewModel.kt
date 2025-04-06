package com.example.musicplayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.SongObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val songObserver: SongObserver
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    private val channel = Channel<MainEvent>()
    val event = channel.receiveAsFlow()

    fun onAction(action: MainAction) {
        when(action) {
            is MainAction.SubmitPermissionsInfo -> {
                var requestRead = false
                var requestPost = false

                if(!action.hasRead && action.shouldShowReadRationale) {
                    requestRead = true
                }

                if(!action.hasPost && action.shouldShowPostRationale) {
                    requestPost = true
                }

                if(requestPost || requestRead) {
                    viewModelScope.launch {
                        channel.send(MainEvent.RequestPermissions)
                    }
                    return
                }

                if(!action.hasRead || !action.hasPost) {
                    state = state.copy(showSettingsDialog = true)
                    return
                }

                state = state.copy(isLoaded = true, showSettingsDialog = false)
                songObserver.startObservingSongs()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        songObserver.stopObservingSongs()
    }
}