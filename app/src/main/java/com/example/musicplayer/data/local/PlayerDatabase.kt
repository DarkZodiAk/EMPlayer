package com.example.musicplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.musicplayer.data.local.dao.FolderDao
import com.example.musicplayer.data.local.dao.PlayerStateDao
import com.example.musicplayer.data.local.dao.SongDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.data.local.entity.Folder
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.SongPlaylistCross
import com.example.musicplayer.data.local.entity.Playlist
import com.example.musicplayer.data.local.entity.SongFolderCross
import com.example.musicplayer.data.local.entity.SongInCurrentPlaylist
import com.example.musicplayer.data.local.entity.SongInInitialPlaylist

@Database(
    entities = [
        Playlist::class,
        Song::class,
        SongPlaylistCross::class,
        Folder::class,
        SongFolderCross::class,
        SongInCurrentPlaylist::class,
        SongInInitialPlaylist::class],
    version = 6
)
abstract class PlayerDatabase: RoomDatabase() {
    abstract val playlistDao: PlaylistDao
    abstract val songDao: SongDao
    abstract val folderDao: FolderDao
    abstract val playerStateDao: PlayerStateDao

    companion object {
        const val DB_NAME = "player_db"
    }
}