package com.example.musicplayer.presentation.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.presentation.components.SongCard
import com.example.musicplayer.presentation.search.components.SearchTextField

@Composable
fun SearchScreenRoot(
    viewModel: SearchViewModel = hiltViewModel(),
    onOpenPlayer: () -> Unit,
    onBack: () -> Unit
) {
    SearchScreen(
        state = viewModel.state,
        onAction = { action ->
            when(action) {
                SearchAction.OnBackClick -> onBack()
                is SearchAction.PlaySong -> onOpenPlayer()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(true) {
        if(state.firstVisit) {
            focusRequester.requestFocus()
            onAction(SearchAction.RequestedFieldFocus)
        }
    }

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp, start = 12.dp, end = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onAction(SearchAction.OnBackClick)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
                Spacer(Modifier.width(8.dp))
                SearchTextField(
                    value = state.searchQuery,
                    onValueChange = {
                        onAction(SearchAction.ModifySearchQuery(it))
                    },
                    focusRequester = focusRequester,
                    modifier = Modifier.weight(1f)
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(start = 8.dp, end = 8.dp)
        ) {
            itemsIndexed(state.songs) { index, song ->
                SongCard(
                    title = song.title,
                    artistName = song.artistName,
                    albumArtUri = song.albumArt,
                    isPlaying = song == state.playingSong,
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onAction(SearchAction.PlaySong(index))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun SearchScreenPreview() {
    SearchScreen(
        state = SearchState(
            searchQuery = "hello",
            songs = listOf(Song(title = "Song1"), Song(title = "Song2"))
        ),
        onAction = {}
    )
}