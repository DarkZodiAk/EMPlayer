package com.example.musicplayer.presentation.player

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardTab
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicplayer.domain.usecases.RepeatMode
import com.example.musicplayer.presentation.player.components.PlayerSlider
import com.example.musicplayer.ui.theme.MusicPlayerTheme


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
    var horizontalSwipeCount by rememberSaveable { mutableFloatStateOf(0f) }
    var verticalSwipeCount by rememberSaveable { mutableFloatStateOf(0f) }

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
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 36.dp)
        ) {
            AsyncImage(
                model = state.playingSong.albumArt.toUri(),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                horizontalSwipeCount = 0f
                                verticalSwipeCount = 0f
                            },
                            onDragEnd = {
                                if(verticalSwipeCount > 128f) {
                                    verticalSwipeCount = 0f
                                    onAction(PlayerAction.OnBackClick)
                                } else if(horizontalSwipeCount < -96f) {
                                    horizontalSwipeCount = 0f
                                    onAction(PlayerAction.OnNextSongClick)
                                } else if(horizontalSwipeCount > 96f) {
                                    horizontalSwipeCount = 0f
                                    onAction(PlayerAction.OnPrevSongClick)
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val (x,y) = dragAmount
                            horizontalSwipeCount += x
                            verticalSwipeCount += y
                        }
                    }
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = state.playingSong.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.playingSong.artistName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            PlayerSlider(
                currentPosition = state.currentProgress,
                endPosition = state.playingSong.duration,
                onValueChangeFinished = { selectedPosition ->
                    onAction(PlayerAction.OnSongPositionSet(selectedPosition.toLong()))
                }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onAction(PlayerAction.SwitchRepeatMode) }) {
                    Icon(
                        imageVector = when(state.repeatMode) {
                            RepeatMode.NO_REPEAT -> Icons.AutoMirrored.Filled.KeyboardTab
                            RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                            RepeatMode.REPEAT_ALL -> Icons.Default.Repeat
                        },
                        contentDescription = null
                    )
                }
                IconButton(onClick = { onAction(PlayerAction.OnPrevSongClick) }) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null)
                }
                IconButton(
                    onClick = { onAction(PlayerAction.OnPlayPauseClick) },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = if(state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = { onAction(PlayerAction.OnNextSongClick) }) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
                }
                IconButton(onClick = { onAction(PlayerAction.SwitchShuffledMode) }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        tint = if(state.isShuffleEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        contentDescription = null
                    )
                }
            }
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