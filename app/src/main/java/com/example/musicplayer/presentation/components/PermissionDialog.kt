package com.example.musicplayer.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PermissionDialog(
    onClick: () -> Unit
) {
    AlertDialog(
        title = {
            Text(text = "Требуется разрешение")
        },
        text = {
            Text(text = "Для того, чтобы проигрывать музыку, нужен доступ к медиафайлам на вашем устройстве")
        },
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = onClick) {
                Text(text = "Открыть настройки")
            }
        }
    )
}