package com.example.musicplayer.presentation.folders

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.local.entity.Folder
import com.example.musicplayer.presentation.components.FolderCard

@Composable
fun FoldersScreenRoot(
    viewModel: FoldersViewModel = hiltViewModel(),
    onFolderClick: (Long) -> Unit
) {
    FoldersScreen(
        folders = viewModel.folders.collectAsState().value,
        onAction = { action ->
            when(action) {
                is FoldersAction.OnFolderClick -> onFolderClick(action.id)
            }
        }
    )
}

@Composable
fun FoldersScreen(
    folders: List<Folder>,
    onAction: (FoldersAction) -> Unit
) {
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
        ) {
            items(
                items = folders,
                key = { folder ->
                    folder.id ?: -1L
                }
            ) { folder ->
                FolderCard(
                    name = folder.name,
                    onClick = { onAction(FoldersAction.OnFolderClick(folder.id!!)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun FoldersScreenPreview() {
    FoldersScreen(
        folders = listOf(Folder()),
        onAction = {}
    )
}