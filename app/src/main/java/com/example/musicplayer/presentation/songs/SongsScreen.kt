package com.example.musicplayer.presentation.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.domain.SortDirection
import com.example.musicplayer.domain.SortType
import com.example.musicplayer.presentation.components.SongCard
import com.example.musicplayer.presentation.components.SortByBottomSheet
import com.example.musicplayer.presentation.util.toText
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
                else -> Unit
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
    var selectSortBySheetIsVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 8.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SortByText(
                        sortType = state.sortType,
                        sortDirection = state.sortDirection,
                        modifier = Modifier
                            .clickable { selectSortBySheetIsVisible = true }
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
            }
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

        if(selectSortBySheetIsVisible) {
            SortByBottomSheet(
                sortType = state.sortType,
                sortDirection = state.sortDirection,
                onAction = { sortType ->
                    onAction(SongsAction.SwitchSortType(sortType = sortType))
                },
                onDismiss = { selectSortBySheetIsVisible = false }
            )
        }
    }
}

@Composable
fun SortByText(
    sortType: SortType,
    sortDirection: SortDirection,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(sortType) {
        text = sortType.toText()
    }
    Row(modifier = modifier) {
        Text(
            text = "Сортировка по: $text",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Icon(
            imageVector = if(sortDirection == SortDirection.ASC) Icons.Default.ArrowDownward
            else Icons.Default.ArrowUpward,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}


@Preview
@Composable
private fun SongsScreenPreview() {
    MusicPlayerTheme {
        SongsScreen(
            state = SongsState(listOf(Audio(title = "OK", artistName = "Artist"))),
            onAction = {}
        )
    }
}