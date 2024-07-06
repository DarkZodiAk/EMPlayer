package com.example.musicplayer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.musicplayer.presentation.player.PlayerScreenRoot
import com.example.musicplayer.presentation.playlist.PlaylistScreenRoot
import com.example.musicplayer.presentation.playlists.PlaylistsScreenRoot
import com.example.musicplayer.presentation.selectSongs.SelectSongsScreenRoot
import com.example.musicplayer.presentation.songs.SongsScreenRoot

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
                SongsScreenRoot(
                    onOpenPlayer = { navController.navigate(Route.PlayerScreen) },
                    onPlaylistsClick = { navController.navigate(Route.PlaylistsScreen) }
                )
            }
            composable<Route.PlaylistsScreen> {
                PlaylistsScreenRoot(
                    onPlaylistClick = {
                        navController.navigate(Route.PlaylistScreen(playlistId = it))
                    },
                    onSongsClick = {
                        navController.navigate(Route.SongsScreen)
                    }
                )
            }
            composable<Route.PlaylistScreen> {
                PlaylistScreenRoot(
                    onOpenPlayer = { navController.navigate(Route.PlayerScreen) },
                    onAddSongsClick = {
                        navController.navigate(Route.SelectSongsScreen(playlistId = it))
                    },
                    onBack = { navController.navigateUp() }
                )
            }
            composable<Route.SelectSongsScreen> {
                SelectSongsScreenRoot(onBack = { navController.navigateUp() })
            }
            composable<Route.PlayerScreen>(
                deepLinks = listOf(navDeepLink { uriPattern = "mplayer://player" })
            ) {
                val context = LocalContext.current
                PlayerScreenRoot(onBack = { navController.navigateUp() })
            }
        }
    }
}