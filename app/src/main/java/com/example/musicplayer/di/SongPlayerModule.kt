package com.example.musicplayer.di

import com.example.musicplayer.data.SongPlayerImpl
import com.example.musicplayer.domain.songPlayer.SongPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SongPlayerModule {
    @Binds
    @Singleton
    abstract fun bindSongPlayerImpl(
        songPlayerImpl: SongPlayerImpl
    ): SongPlayer
}