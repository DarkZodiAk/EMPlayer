package com.example.musicplayer.presentation.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.presentation.components.NavBar

@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel = hiltViewModel(),
    onPlaylistClick: (Long) -> Unit,
    navigate: (String) -> Unit
) {
    val playlists = viewModel.playlists.collectAsState()
    var dialogIsVisible by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }


    if(dialogIsVisible){
        AlertDialog(
            onDismissRequest = { dialogIsVisible = false },
            title = {
                Text(text = "Имя плейлиста")
            },
            text = {
                TextField(value = playlistName, onValueChange = { playlistName = it })
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.onEvent(PlaylistsEvent.NewPlaylist(playlistName))
                    dialogIsVisible = false
                    playlistName = ""
                }) {
                    Text(text = "Создать")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { dialogIsVisible = false }) {
                    Text(text = "Отмена")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavBar(
                onClick = { navigate(it) },
                index = 1
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dialogIsVisible = true }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Create playlist")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Новый плейлист")
                }
            }
            items(playlists.value) { playlist ->
                Text(
                    text = playlist.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaylistClick(playlist.id!!) }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}