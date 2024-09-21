package com.example.musicplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.musicplayer.data.local.entity.Audio
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {

    @Upsert
    suspend fun upsertAudio(audio: Audio)

    @Delete
    suspend fun deleteAudio(audio: Audio)

    @Query("SELECT * FROM audio")
    fun getAllAudio(): Flow<List<Audio>>
}