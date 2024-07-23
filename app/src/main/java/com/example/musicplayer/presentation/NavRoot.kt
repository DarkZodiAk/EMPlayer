package com.example.musicplayer.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.musicplayer.presentation.home.HomeScreenRoot
import com.example.musicplayer.presentation.player.PlayerScreenRoot
import com.example.musicplayer.presentation.playlist.PlaylistScreenRoot
import com.example.musicplayer.presentation.search.SearchScreenRoot
import com.example.musicplayer.presentation.selectSongs.SelectSongsScreenRoot


@Composable
fun NavRoot(
    isLoaded: Boolean,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    if(isLoaded) {
        NavHost(
            navController = navController,
            startDestination = Route.HomeScreen,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            modifier = modifier
        ) {
            composable<Route.HomeScreen> {
                HomeScreenRoot(
                    onOpenPlayer = { navController.navigate(Route.PlayerScreen) },
                    onOpenSearch = { navController.navigate(Route.SearchScreen) },
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
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(300)
                    )
                }
            ) {
                PlayerScreenRoot(onBack = { navController.navigateUp() })
            }
            composable<Route.SearchScreen> {
                SearchScreenRoot()
            }
        }
    }
}