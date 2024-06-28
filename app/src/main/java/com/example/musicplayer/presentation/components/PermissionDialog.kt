package com.example.musicplayer.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PermissionDialog(
    title: String,
    text: String,
    buttonText: String,
    onClick: () -> Unit
) {
    AlertDialog(
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = onClick) {
                Text(text = buttonText)
            }
        }
    )
}