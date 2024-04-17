package com.example.musicplayer.presentation.selectSongs

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.presentation.components.SongCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSongsScreen(
    viewModel: SelectSongsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val songs = viewModel.songs.collectAsState()
    val selectedSongs = viewModel.selectedSongs

    LaunchedEffect(selectedSongs) {
        Log.d("UPDATE SELECTED", selectedSongs.toString())
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
                    Text(text = "Добавить песни")
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onEvent(SelectSongsEvent.Confirm)
                            onBack()
                        }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(songs.value) { song ->
                SongCard(
                    song = song,
                    onClick = {
                        if(song.id in selectedSongs) {
                            viewModel.onEvent(SelectSongsEvent.DeleteSong(song.id))
                        } else {
                            viewModel.onEvent(SelectSongsEvent.AddSong(song.id))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        RadioButton(
                            selected = song.id in selectedSongs,
                            onClick = {
                                if(song.id in selectedSongs) {
                                    viewModel.onEvent(SelectSongsEvent.DeleteSong(song.id))
                                } else {
                                    viewModel.onEvent(SelectSongsEvent.AddSong(song.id))
                                }
                            },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                )
            }
        }
    }
}