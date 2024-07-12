package com.example.musicplayer.presentation.player

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.presentation.parseDuration
import com.example.musicplayer.presentation.player.components.PlayerSlider
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import dev.vivvvek.seeker.Seeker
import dev.vivvvek.seeker.SeekerDefaults


@Composable
fun PlayerScreenRoot(
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                PlayerEvent.Error -> {
                    Toast.makeText(
                        context,
                        "Что-то пошло не так",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    PlayerScreen(
        state = viewModel.state,
        onAction = { action ->
            when(action) {
                PlayerAction.OnBackClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {  },
                navigationIcon = {
                    IconButton(onClick = { onAction(PlayerAction.OnBackClick) }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = state.playingSong.title)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.playingSong.artistName)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = parseDuration(state.currentProgress))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { onAction(PlayerAction.OnPrevSongClick) }) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null)
                }
                IconButton(onClick = { onAction(PlayerAction.OnPlayPauseClick) }) {
                    Icon(
                        imageVector = if(state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                }
                IconButton(onClick = { onAction(PlayerAction.OnNextSongClick) }) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            PlayerSlider(
                currentPosition = state.currentProgress,
                endPosition = state.playingSong.duration,
                onValueChangeFinished = { selectedPosition ->
                    onAction(PlayerAction.OnSongPositionSet(selectedPosition.toLong()))
                }
            )
        }
    }
}

@Preview
@Composable
private fun PlayerScreenPreview() {
    MusicPlayerTheme {
        PlayerScreen(
            state = PlayerState(),
            onAction = {}
        )
    }
}