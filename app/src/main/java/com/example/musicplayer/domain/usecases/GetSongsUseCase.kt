package com.example.musicplayer.domain.usecases

import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.PlayerRepository
import com.example.musicplayer.domain.SortDirection
import com.example.musicplayer.domain.SortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSongsUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    operator fun invoke(sortType: SortType, sortDirection: SortDirection): Flow<List<Song>> {
        return repository.getAllSongs()
            .map {
                when(sortType) {
                    SortType.TITLE -> it.sortedBy { it.title }
                    SortType.ARTIST -> it.sortedBy { it.artistName }
                    SortType.ALBUM -> it.sortedBy { it.albumName }
                    SortType.DURATION -> it.sortedBy { it.duration }
                    SortType.DATE_ADDED -> it.sortedBy { it.dateAdded }
                    SortType.DATE_MODIFIED -> it.sortedBy { it.dateModified }
                }
            }.map {
                when(sortDirection) {
                    SortDirection.ASC -> it
                    SortDirection.DESC -> it.reversed()
                }
            }
    }
}