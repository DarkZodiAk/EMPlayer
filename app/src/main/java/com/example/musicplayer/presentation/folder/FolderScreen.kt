package com.example.musicplayer.presentation.folder

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.presentation.components.SongCard

@Composable
fun FolderScreenRoot(
    viewModel: FolderViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    FolderScreen(
        state = viewModel.state,
        onAction = { action ->
            when(action) {
                FolderAction.OnBack -> onBack()
                is FolderAction.OnSongClick -> onOpenPlayer()
            }
            viewModel.onAction(action)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    state: FolderState,
    onAction: (FolderAction) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onAction(FolderAction.OnBack) }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text(
                        text = state.folder.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
        ) {
            itemsIndexed(
                items = state.songs,
                key = { _, song ->
                    song.id
                }
            ) { index, song ->
                SongCard(
                    title = song.title,
                    artistName = song.artistName,
                    albumArtUri = song.albumArt,
                    isPlaying = song == state.playingSong,
                    onClick = { onAction(FolderAction.OnSongClick(index)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun FolderScreenPreview() {
    FolderScreen(
        state = FolderState(),
        onAction = {}
    )
}