package com.example.musicplayer.presentation.songs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.presentation.Route
import com.example.musicplayer.presentation.components.NavBar
import com.example.musicplayer.presentation.components.SongCard
import com.example.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun SongsScreenRoot(
    viewModel: SongsViewModel = hiltViewModel(),
    onOpenPlayer: () -> Unit,
    onPlaylistsClick: () -> Unit
) {
    SongsScreen(
        songs = viewModel.songs.collectAsStateWithLifecycle().value,
        onAction = { action ->
            when(action) {
                SongsAction.OnPlaylistsClick -> onPlaylistsClick()
                is SongsAction.PlaySong -> onOpenPlayer()
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun SongsScreen(
    songs: List<Audio>,
    onAction: (SongsAction) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavBar(
                onClick = { if(it is Route.PlaylistsScreen) onAction(SongsAction.OnPlaylistsClick) },
                index = 0
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            itemsIndexed(
                items = songs,
                key = { _, song ->
                    song.id
                }
            ) { index, song ->
                SongCard(
                    song = song,
                    onClick = { onAction(SongsAction.PlaySong(index)) },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview
@Composable
private fun SongsScreenPreview() {
    MusicPlayerTheme {
        SongsScreen(
            songs = emptyList(),
            onAction = {}
        )
    }
}