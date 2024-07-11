package com.example.musicplayer.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.musicplayer.presentation.player.PlayerScreenRoot
import com.example.musicplayer.presentation.playlist.PlaylistScreenRoot
import com.example.musicplayer.presentation.selectSongs.SelectSongsScreenRoot
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach

@Composable
fun NavRoot(
    isLoaded: Boolean,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    if(isLoaded) {
        NavHost(
            navController = navController,
            startDestination = Route.MainScreen,
            modifier = modifier
        ) {
            composable<Route.MainScreen> {
                MainScreenRoot(
                    songsOnOpenPlayer = { navController.navigate(Route.PlayerScreen) },
                    playlistsOnPlaylistClick = { navController.navigate(Route.PlaylistScreen(playlistId = it)) }
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
                deepLinks = listOf(navDeepLink { uriPattern = "mplayer://player" }),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(300)
                    )
                }
            ) {
                PlayerScreenRoot(onBack = { navController.navigateUp() })
            }
        }
    }
}