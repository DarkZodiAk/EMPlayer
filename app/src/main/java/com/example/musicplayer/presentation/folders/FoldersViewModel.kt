package com.example.musicplayer.presentation.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val repository: PlayerRepository
) : ViewModel() {
    val folders = repository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}