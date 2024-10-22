package com.example.musicplayer.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.room.Room
import com.example.musicplayer.data.local.PlayerDatabase
import com.example.musicplayer.data.local.dao.FolderDao
import com.example.musicplayer.data.local.dao.PlayerStateDao
import com.example.musicplayer.data.local.dao.SongDao
import com.example.musicplayer.data.local.dao.PlaylistDao
import com.example.musicplayer.domain.PlayerRepository
import com.example.musicplayer.domain.PlaylistManager
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
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providesPlayerDao(db: PlayerDatabase): PlaylistDao {
        return db.playlistDao
    }

    @Provides
    @Singleton
    fun providesSongDao(db: PlayerDatabase): SongDao {
        return db.songDao
    }

    @Provides
    @Singleton
    fun providesFolderDao(db: PlayerDatabase): FolderDao {
        return db.folderDao
    }

    @Provides
    @Singleton
    fun providesPlayerStateDao(db: PlayerDatabase): PlayerStateDao {
        return db.playerStateDao
    }

    @Provides
    @Singleton
    fun providesExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @Provides
    @Singleton
    fun providesMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession {
        return MediaSession.Builder(context, player).build()
    }

    @Provides
    @Singleton
    fun providesPlaylistManager(
        playerRepository: PlayerRepository
    ): PlaylistManager {
        return PlaylistManager(playerRepository)
    }
}