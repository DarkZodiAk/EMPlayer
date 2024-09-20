package com.example.musicplayer.domain.usecases

import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.domain.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    operator fun invoke(searchQuery: String): Flow<List<Audio>> {
        return repository.getAllAudio().map {
            it.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) || song.artistName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}