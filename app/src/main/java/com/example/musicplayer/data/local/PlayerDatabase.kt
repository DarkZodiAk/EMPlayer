package com.example.musicplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.musicplayer.data.local.dao.AudioDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.data.local.entity.AudioPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist

@Database(
    entities = [Playlist::class, Audio::class, AudioPlaylistCross::class],
    version = 4
)
abstract class PlayerDatabase: RoomDatabase() {
    abstract val playlistDao: PlaylistDao
    abstract val audioDao: AudioDao

    companion object {
        const val DB_NAME = "player_db"
    }
}