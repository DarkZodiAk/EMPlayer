package com.example.musicplayer.di

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.example.musicplayer.data.AudioObserver
import com.example.musicplayer.data.AudioPlayer
import com.example.musicplayer.data.PermissionObserver
import com.example.musicplayer.data.PlayerRepositoryImpl
import com.example.musicplayer.data.local.PlayerDatabase
import com.example.musicplayer.data.local.dao.AudioDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.domain.PlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun providesPlayerDatabase(@ApplicationContext context: Context): PlayerDatabase {
        return Room.databaseBuilder(
            context,
            PlayerDatabase::class.java,
            PlayerDatabase.DB_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providesPlayerDao(db: PlayerDatabase): PlaylistDao {
        return db.playlistDao
    }

    @Provides
    @Singleton
    fun providesAudioDao(db: PlayerDatabase): AudioDao {
        return db.audioDao
    }

    @Provides
    @Singleton
    fun providesPlayerRepository(
        playlistDao: PlaylistDao,
        audioDao: AudioDao
    ): PlayerRepository {
        return PlayerRepositoryImpl(playlistDao, audioDao)
    }

    @Provides
    @Singleton
    fun providesAudioObserver(
        @ApplicationContext context: Context,
        playerRepository: PlayerRepository
    ): AudioObserver {
        return AudioObserver(context, playerRepository)
    }

    @Provides
    @Singleton
    fun providesPermissionObserver(@ApplicationContext context: Context): PermissionObserver {
        return PermissionObserver(context)
    }

    @Provides
    @Singleton
    fun providesExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val player = ExoPlayer.Builder(context).build()
        player.repeatMode = Player.REPEAT_MODE_ALL
        return player
    }

    @Provides
    @Singleton
    fun providesAudioPlayer(exoPlayer: ExoPlayer): AudioPlayer {
        return AudioPlayer(exoPlayer)
    }
}