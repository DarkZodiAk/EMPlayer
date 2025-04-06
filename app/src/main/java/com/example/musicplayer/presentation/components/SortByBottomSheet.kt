package com.example.musicplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.musicplayer.domain.SortDirection
import com.example.musicplayer.domain.SortType
import com.example.musicplayer.presentation.util.toText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortByBottomSheet(
    sortType: SortType,
    sortDirection: SortDirection,
    onAction: (SortType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = modalSheetState,
        onDismissRequest = onDismiss,
        modifier = modifier,
        dragHandle = {
            Column {
                Text(
                    text = "Сортировка",
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    ) {
        Column {
            SortType.entries.forEach { type ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if(type == sortType) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Unspecified)
                        .clickable {
                            onAction(type)
                        }
                        .padding(vertical = 16.dp, horizontal = 24.dp)
                ) {
                    Text(text = type.toText())
                    if(type == sortType) {
                        Icon(
                            if(sortDirection == SortDirection.ASC) Icons.Default.ArrowDownward
                            else Icons.Default.ArrowUpward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}