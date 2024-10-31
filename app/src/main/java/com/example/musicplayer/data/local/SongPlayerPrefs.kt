package com.example.musicplayer.data.local

import android.content.Context
import androidx.core.content.edit
import com.example.musicplayer.data.local.dao.SongDao
import com.example.musicplayer.domain.songPlayer.SongPlayerState
import com.example.musicplayer.domain.usecases.RepeatMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongPlayerPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao
) {
    private val pref = context.getSharedPreferences("NAME", Context.MODE_PRIVATE)

    fun savePlayerState(state: SongPlayerState) {
        if(state == SongPlayerState.empty) return
        pref.edit {
            state.currentSong?.let { song ->
                putLong(CURRENT_SONG_ID, song.id)
            }
            putLong(CURRENT_POSITION, state.currentPosition)
            putString(REPEAT_MODE, state.repeatMode.toString())
            putBoolean(SHUFFLE_ENABLED, state.isShuffleEnabled)
        }
    }

    suspend fun getPlayerState(): SongPlayerState? {
        val currentSongId = pref.getLong(CURRENT_SONG_ID, -1L)
        val currentPosition = pref.getLong(CURRENT_POSITION, 0)
        val repeatMode = pref.getString(REPEAT_MODE, RepeatMode.NO_REPEAT.toString())
        val shuffleEnabled = pref.getBoolean(SHUFFLE_ENABLED, false)

        val song = songDao.getSongById(currentSongId)

        return if(currentSongId != -1L && song != null && repeatMode != null) {
            SongPlayerState(
                currentSong = song,
                currentPosition = currentPosition,
                repeatMode = RepeatMode.valueOf(repeatMode),
                isShuffleEnabled = shuffleEnabled
            )
        } else null
    }

    fun savePlaylistIndex(index: Int) {
        pref.edit {
            putInt(PLAYLIST_INDEX, index)
        }
    }

    fun getPlaylistIndex(): Int {
        return pref.getInt(PLAYLIST_INDEX, 0)
    }

    companion object {
        private const val CURRENT_SONG_ID = "CURRENT_SONG_ID"
        private const val CURRENT_POSITION = "CURRENT_POSITION"
        private const val REPEAT_MODE = "REPEAT_MODE"
        private const val SHUFFLE_ENABLED = "SHUFFLE_ENABLED"
        private const val PLAYLIST_INDEX = "PLAYLIST_INDEX"
    }
}