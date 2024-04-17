package com.example.musicplayer.presentation.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DefaultDropDownMenu(
    actions: HashMap<String, () -> Unit>,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = {
            onDismissRequest()
        },
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                text = {
                    Text(text = action.key)
                },
                onClick = {
                    action.value() //ОБЯЗАТЕЛЬНО СКОБКИ - ВЫЗОВ ФУНКЦИИ
                    onDismissRequest()
                })
        }
    }
}