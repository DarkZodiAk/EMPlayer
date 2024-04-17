package com.example.musicplayer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.musicplayer.presentation.player.PlayerScreen
import com.example.musicplayer.presentation.playlist.PlaylistScreen
import com.example.musicplayer.presentation.playlists.PlaylistsScreen
import com.example.musicplayer.presentation.selectSongs.SelectSongsScreen
import com.example.musicplayer.presentation.songs.SongsScreen

@Composable
fun NavRoot(
    isLoaded: Boolean,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    if(isLoaded) {
        NavHost(
            navController = navController,
            startDestination = Screen.SongsScreen.route,
            modifier = modifier
        ) {
            composable(Screen.SongsScreen.route) {
                SongsScreen(
                    onOpenPlayer = { navController.navigate(Screen.PlayerScreen.route) },
                    navigate = { navController.navigate(it) }
                )
            }
            composable(Screen.PlaylistsScreen.route) {
                PlaylistsScreen(
                    onPlaylistClick = { navController.navigate(Screen.PlaylistScreen.route + "?id=$it") },
                    navigate = { navController.navigate(it) }
                )
            }
            composable(
                route = Screen.PlaylistScreen.route + "?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.LongType
                    }
                )
            ) {
                PlaylistScreen(
                    onOpenPlayer = { navController.navigate(Screen.PlayerScreen.route) },
                    onAddSongsClick = { navController.navigate(Screen.SelectSongsScreen.route + "?playlistId=$it") },
                    onBack = { navController.navigateUp() }
                )
            }
            composable(
                route = Screen.SelectSongsScreen.route + "?playlistId={playlistId}",
                arguments = listOf(
                    navArgument("playlistId") {
                        type = NavType.LongType
                    }
                )
            ) {
                SelectSongsScreen(onBack = { navController.navigateUp() })
            }
            composable(Screen.PlayerScreen.route) {
                PlayerScreen(
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}