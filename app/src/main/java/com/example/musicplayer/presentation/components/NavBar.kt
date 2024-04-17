package com.example.musicplayer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.musicplayer.presentation.Screen

@Composable
fun NavBar(
    onClick: (String) -> Unit,
    index: Int
) {
    val items = listOf(
        BottomNavigationItem(
            title = "Песни",
            destination = Screen.SongsScreen.route,
            icon = Icons.Filled.LibraryMusic
        ),
        BottomNavigationItem(
            title = "Плейлисты",
            destination = Screen.PlaylistsScreen.route,
            icon = Icons.Filled.FolderCopy
        )
    )

    var selectedItemIndex by remember {
        mutableIntStateOf(index)
    }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    onClick(item.destination)
                },
                label = {
                    Text(text = item.title)
                },
                icon = {
                    Box{
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                        )
                    }
                }
            )
        }
    }
}

data class BottomNavigationItem(
    val title: String,
    val destination: String,
    val icon: ImageVector
)