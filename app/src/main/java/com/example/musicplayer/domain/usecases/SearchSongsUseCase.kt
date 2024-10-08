package com.example.musicplayer.domain.usecases

import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    operator fun invoke(searchQuery: String): Flow<List<Song>> {
        return repository.getAllSongs().map {
            it.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) || song.artistName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}