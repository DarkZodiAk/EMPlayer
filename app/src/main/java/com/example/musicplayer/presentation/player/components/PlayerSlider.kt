package com.example.musicplayer.presentation.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.musicplayer.presentation.parseDuration
import dev.vivvvek.seeker.Seeker
import dev.vivvvek.seeker.SeekerDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerSlider(
    currentPosition: Long,
    endPosition: Long,
    onValueChangeFinished: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPosition by remember { mutableFloatStateOf(0f) }
    var isSelectingPosition by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        Seeker(
            value = if (isSelectingPosition) selectedPosition else currentPosition.toFloat(),
            onValueChange = {
                isSelectingPosition = true
                selectedPosition = it
            },
            onValueChangeFinished = {
                scope.launch {
                    onValueChangeFinished(selectedPosition)
                    delay(100L)
                    isSelectingPosition = false
                }
            },
            range = 0f..endPosition.toFloat(),
            dimensions = SeekerDefaults.seekerDimensions(
                trackHeight = 3.dp,
                thumbRadius = 6.dp
            ),
            colors = SeekerDefaults.seekerColors(
                progressColor = Color.White,
                trackColor = Color.DarkGray,
                thumbColor = Color.White
            ),
            modifier = Modifier.height(24.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = parseDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = parseDuration(endPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}