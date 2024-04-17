package com.example.musicplayer

import android.Manifest
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.AudioObserver
import com.example.musicplayer.data.PermissionObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val audioObserver: AudioObserver,
    private val permissionObserver: PermissionObserver
) : ViewModel() {
    var hasReadPermission by mutableStateOf(false)
        private set
    var isLoaded by mutableStateOf(false)
        private set

    private val channel = Channel<UiMainEvent>()
    val uiPermissionChannel = channel.receiveAsFlow()

    init {
        checkReadPermission()
    }

    fun checkReadPermission() {
        hasReadPermission = permissionObserver.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        Log.d("PERMISSION GRANTED:", "$hasReadPermission")
        if(!hasReadPermission) {
            openPermissionDialog()
        } else {
            launchAudioObserver()
        }
    }

    private fun openPermissionDialog() {
        viewModelScope.launch {
            channel.send(UiMainEvent.AskForPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun launchAudioObserver() {
        viewModelScope.launch {
            audioObserver.observeAudio().collect {
                isLoaded = it
            }
        }
    }
}