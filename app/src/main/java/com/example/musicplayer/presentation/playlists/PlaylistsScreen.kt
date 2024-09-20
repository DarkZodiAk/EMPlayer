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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.presentation.components.PlaylistCard
import com.example.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun PlaylistsScreenRoot(
    viewModel: PlaylistsViewModel = hiltViewModel(),
    onPlaylistClick: (Long) -> Unit
) {
    PlaylistsScreen(
        playlists = viewModel.playlists.collectAsStateWithLifecycle().value,
        onAction = { action ->
            when(action) {
                is PlaylistsAction.OnPlaylistClick -> onPlaylistClick(action.id)
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onAction: (PlaylistsAction) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var dialogIsVisible by rememberSaveable { mutableStateOf(false) }
    var playlistName by rememberSaveable { mutableStateOf("") }

    if(dialogIsVisible){
        AlertDialog(
            onDismissRequest = { dialogIsVisible = false },
            title = {
                Text(text = "Имя плейлиста")
            },
            text = {
                TextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    supportingText = {
                        if(playlistName.isBlank())
                            Text(text = "Имя не должно быть пустым")
                    },
                    modifier = Modifier.focusRequester(focusRequester)
                )
            },
            confirmButton = {
                Button(
                    enabled = playlistName.isNotBlank(),
                    onClick = {
                        onAction(PlaylistsAction.OnCreatePlaylistClick(playlistName))
                        dialogIsVisible = false
                        playlistName = ""
                    }
                ) {
                    Text(text = "Создать")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { dialogIsVisible = false }) {
                    Text(text = "Отмена")
                }
            }
        )

        LaunchedEffect(true) {
            focusRequester.requestFocus()
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
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
            items(
                items = playlists,
                key = { playlist ->
                    playlist.id ?: -1
                }
            ) { playlist ->
                PlaylistCard(
                    name = playlist.name,
                    songsCount = "${playlist.songsCount} песен",
                    imageUri = playlist.imageUri,
                    onClick = { onAction(PlaylistsAction.OnPlaylistClick(playlist.id!!)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun PlaylistsScreenPreview() {
    MusicPlayerTheme {
        PlaylistsScreen(
            playlists = listOf(Playlist(123L, "Playlist1", songsCount = 158)),
            onAction = { }
        )
    }
}