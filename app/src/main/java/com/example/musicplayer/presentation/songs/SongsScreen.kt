package com.example.musicplayer.presentation.songs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.presentation.components.SongCard
import com.example.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun SongsScreenRoot(
    viewModel: SongsViewModel = hiltViewModel(),
    onOpenPlayer: () -> Unit
) {
    SongsScreen(
        state = viewModel.state,
        onAction = { action ->
            when(action) {
                is SongsAction.PlaySong -> onOpenPlayer()
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun SongsScreen(
    state: SongsState,
    onAction: (SongsAction) -> Unit
) {
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
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
                    onClick = { onAction(SongsAction.PlaySong(index)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun SongsScreenPreview() {
    MusicPlayerTheme {
        SongsScreen(
            state = SongsState(),
            onAction = {}
        )
    }
}