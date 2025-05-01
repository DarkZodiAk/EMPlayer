package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.musicplayer.data.local.entity.Folder
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.SongFolderCross
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Upsert
    suspend fun upsertFolder(folder: Folder): Long

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM folder")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folder WHERE id = :id")
    fun getFolderById(id: Long): Flow<Folder?>

    @Query("SELECT id FROM folder WHERE absoluteName = :absoluteName")
    suspend fun getFolderIdByAbsoluteName(absoluteName: String): Long?

    @Query("SELECT * FROM song WHERE id IN (SELECT songId FROM songfoldercross WHERE folderId = :folderId)")
    fun getSongsFromFolder(folderId: Long): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToFolder(songFolderCross: SongFolderCross)

    @Delete
    suspend fun deleteSongFromFolder(songFolderCross: SongFolderCross)
}