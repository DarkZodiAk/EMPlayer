package com.example.musicplayer.di

import com.example.musicplayer.data.PlayerRepositoryImpl
import com.example.musicplayer.domain.PlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerBinder {
    @Binds
    @Singleton
    abstract fun bindsPlayerRepository(
        playerRepositoryImpl: PlayerRepositoryImpl
    ): PlayerRepository
}