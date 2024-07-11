package com.example.musicplayer.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.musicplayer.presentation.playlists.PlaylistsScreenRoot
import com.example.musicplayer.presentation.songs.SongsScreenRoot
import kotlinx.coroutines.launch


@Composable
fun MainScreenRoot(
    songsOnOpenPlayer: () -> Unit,
    playlistsOnPlaylistClick: (Long) -> Unit,
) {
    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }
    val pagerState = rememberPagerState(pageCount = { TabItem.entries.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                TabItem.entries.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            selectedTabIndex = index
                        },
                        text = {
                            Text(text = tabItem.name)
                        }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { index ->
                when (index) {
                    TabItem.SONGS.ordinal -> SongsScreenRoot(onOpenPlayer = songsOnOpenPlayer)
                    TabItem.PLAYLISTS.ordinal -> PlaylistsScreenRoot(onPlaylistClick = playlistsOnPlaylistClick)
                }
            }
        }

    }
}

enum class TabItem(val title: String) {
    SONGS("Песни"), PLAYLISTS("Плейлисты")
}