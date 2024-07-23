package com.example.musicplayer.presentation.search

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SearchScreenRoot(
    viewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    SearchScreen(
        state = viewModel,
        onAction = { action ->

        }
    )
}

@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit
) {

}