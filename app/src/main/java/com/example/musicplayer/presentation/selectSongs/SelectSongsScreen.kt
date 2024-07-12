package com.example.musicplayer.presentation.selectSongs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.presentation.components.SongCard
import com.example.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun SelectSongsScreenRoot(
    viewModel: SelectSongsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    SelectSongsScreen(
        state = viewModel.state,
        onAction = { action ->
            when(action) {
                SelectSongsAction.OnBackClick -> onBack()
                SelectSongsAction.OnConfirmClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSongsScreen(
    state: SelectSongsState,
    onAction: (SelectSongsAction) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onAction(SelectSongsAction.OnBackClick) }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text(text = "Добавить песни")
                },
                actions = {
                    IconButton(
                        onClick = { onAction(SelectSongsAction.OnConfirmClick) })
                    {
                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 8.dp)
        ) {
            items(
                items = state.songs,
                key = { song ->
                    song.id
                }
            ) { song ->
                SongCard(
                    title = song.title,
                    artistName = song.artistName,
                    albumArtUri = song.albumArt,
                    onClick = { songSelection(song.id, state.selectedSongs, onAction) },
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        RadioButton(
                            selected = song.id in state.selectedSongs,
                            onClick = { songSelection(song.id, state.selectedSongs, onAction) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                )
            }
        }
    }
}

private fun songSelection(songId: Long, selectedSongs: List<Long>, onSelect: (SelectSongsAction) -> Unit) {
    if(songId in selectedSongs) {
        onSelect(SelectSongsAction.DeleteSong(songId))
    } else {
        onSelect(SelectSongsAction.AddSong(songId))
    }
}

@Preview
@Composable
private fun SelectSongsScreenPreview() {
    MusicPlayerTheme {
        SelectSongsScreen(
            state = SelectSongsState(),
            onAction = {}
        )
    }
}