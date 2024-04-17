package com.example.musicplayer.presentation.songs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.presentation.components.NavBar
import com.example.musicplayer.presentation.components.SongCard

@Composable
fun SongsScreen(
    viewModel: SongsViewModel = hiltViewModel(),
    onOpenPlayer: () -> Unit,
    navigate: (String) -> Unit
) {
    val songs = viewModel.songs.collectAsState()

    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                UiSongsEvent.OpenPlayer -> onOpenPlayer()
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavBar(
                onClick = { navigate(it) },
                index = 0
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            itemsIndexed(songs.value) { index, song ->
                SongCard(
                    song = song,
                    onClick = { viewModel.onEvent(SongsEvent.PlaySong(index)) },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    }

}