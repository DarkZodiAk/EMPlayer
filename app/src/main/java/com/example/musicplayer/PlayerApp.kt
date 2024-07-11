package com.example.musicplayer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.size.Precision
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlayerApp: Application()