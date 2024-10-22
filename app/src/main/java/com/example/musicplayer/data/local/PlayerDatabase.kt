package com.example.musicplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.musicplayer.data.local.dao.SongDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.SongPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.data.local.entity.SongInCurrentPlaylist
import com.example.musicplayer.data.local.entity.SongInInitialPlaylist

@Database(
    entities = [Playlist::class, Song::class, SongPlaylistCross::class, SongInCurrentPlaylist::class, SongInInitialPlaylist::class],
    version = 5
)
abstract class PlayerDatabase: RoomDatabase() {
    abstract val playlistDao: PlaylistDao
    abstract val songDao: SongDao

    companion object {
        const val DB_NAME = "player_db"
    }
}