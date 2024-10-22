package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.data.local.entity.Folder
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.SongFolderCross
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert
    suspend fun insertFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM folder")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folder WHERE id = :id")
    fun getFolderById(id: Long): Flow<Folder?>

    @Query("SELECT * FROM song WHERE id IN (SELECT songId FROM songfoldercross WHERE folderId = :folderId)")
    fun getSongsFromFolder(folderId: Long): Flow<List<Song>>

    @Insert
    suspend fun addSongToFolder(songFolderCross: SongFolderCross)

    @Delete
    suspend fun deleteSongFromFolder(songFolderCross: SongFolderCross)
}