package com.example.musicplayer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
            startDestination = Route.SongsScreen,
            modifier = modifier
        ) {
            composable<Route.SongsScreen> {
                SongsScreen(
                    onOpenPlayer = { navController.navigate(Route.PlayerScreen) },
                    navigate = { navController.navigate(it) }
                )
            }
            composable<Route.PlaylistsScreen> {
                PlaylistsScreen(
                    onPlaylistClick = {
                        navController.navigate(Route.PlaylistScreen(playlistId = it))
                    },
                    navigate = { navController.navigate(it) }
                )
            }
            composable<Route.PlaylistScreen> {
                PlaylistScreen(
                    onOpenPlayer = { navController.navigate(Route.PlayerScreen) },
                    onAddSongsClick = {
                        navController.navigate(Route.SelectSongsScreen(playlistId = it))
                    },
                    onBack = { navController.navigateUp() }
                )
            }
            composable<Route.SongsScreen> {
                SelectSongsScreen(onBack = { navController.navigateUp() })
            }
            composable<Route.PlayerScreen> {
                PlayerScreen(
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}