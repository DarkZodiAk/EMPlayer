package com.example.musicplayer.presentation.playlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.presentation.components.DefaultDropDownMenu
import com.example.musicplayer.presentation.components.SongCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel = hiltViewModel(),
    onAddSongsClick: (Long) -> Unit,
    onOpenPlayer: () -> Unit,
    onBack: () -> Unit
) {
    val playlist = viewModel.playlist
    val songs = viewModel.songs

    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                UiPlaylistEvent.OpenPlayer -> onOpenPlayer()
            }
        }
    }

    var dropdownMenuIsVisible by rememberSaveable { mutableStateOf(false) }
    var deleteDialogIsVisible by rememberSaveable { mutableStateOf(false) }
    var renameDialogIsVisible by rememberSaveable { mutableStateOf(false) }

    var newName by rememberSaveable { mutableStateOf("") }
    
    var selectedSongIdDropdown by rememberSaveable { mutableStateOf<Long?>(null) }
    
    if(renameDialogIsVisible) {
        AlertDialog(
            title = {
                Text(text = "Переименовать плейлист")
            },
            text = {
                TextField(
                    value = newName, 
                    onValueChange = { newName = it },
                    supportingText = {
                        if(newName.isBlank())
                            Text(text = "Имя не должно быть пустым")
                    }
                )
            },
            onDismissRequest = { renameDialogIsVisible = false }, 
            confirmButton = { 
                Button(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        viewModel.onEvent(PlaylistEvent.RenamePlaylist(newName))
                        renameDialogIsVisible = false
                        newName = ""
                    }
                ) {
                    Text(text = "Применить")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { renameDialogIsVisible = false }) {
                    Text(text = "Отмена")
                }
            }
        )
    }
    
    if(deleteDialogIsVisible) {
        AlertDialog(
            title = {
                Text(text = "Удалить плейлист?")
            },
            onDismissRequest = { deleteDialogIsVisible = false },
            dismissButton = {
                OutlinedButton(onClick = { deleteDialogIsVisible = false }) {
                    Text(text = "Нет")
                }
            },
            confirmButton = {
                Button(onClick = {
                    deleteDialogIsVisible = false
                    viewModel.onEvent(PlaylistEvent.DeletePlaylist)
                    onBack()
                }) {
                    Text(text = "Да")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = { 
                    Text(
                        text = playlist.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedSongIdDropdown = null
                            dropdownMenuIsVisible = true
                        }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        if(dropdownMenuIsVisible) {
                            DefaultDropDownMenu(
                                actions = hashMapOf(
                                    "Добавить песни" to { onAddSongsClick(playlist.id!!) },
                                    "Переименовать" to {
                                        dropdownMenuIsVisible = false
                                        renameDialogIsVisible = true
                                    },
                                    "Удалить" to {
                                        dropdownMenuIsVisible = false
                                        deleteDialogIsVisible = true
                                    }
                                ),
                                onDismissRequest = { dropdownMenuIsVisible = false }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            itemsIndexed(
                items = songs,
                key = { index, _ ->
                    index
                }
            ) { index, song ->
                SongCard(
                    song = song,
                    onClick = { viewModel.onEvent(PlaylistEvent.PlaySong(index)) },
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        IconButton(
                            onClick = {
                                dropdownMenuIsVisible = false
                                selectedSongIdDropdown = song.id
                            },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        }

                        if(selectedSongIdDropdown == song.id){
                            DefaultDropDownMenu(
                                actions = hashMapOf(
                                    "Удалить" to { viewModel.onEvent(PlaylistEvent.RemoveSongFromPlaylist(song.id)) }
                                ),
                                onDismissRequest = { selectedSongIdDropdown = null }
                            )
                        }
                    }
                )
            }
        }
    }
}