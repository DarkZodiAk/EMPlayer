package com.example.musicplayer.data

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumArtFetcher @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val contentResolver = context.contentResolver
    private val packageName = context.packageName
    private val defaultImageUri = "android.resource://$packageName/drawable/music_icon"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val semaphore = Semaphore(Int.MAX_VALUE, Int.MAX_VALUE)
    private val mutex = Mutex()

    private var awaitsLeft = MAX_AWAITS
    var working = false

    private val inputAlbumIds = LinkedList<Long>() // Input queue
    private val pendingAlbumIds = mutableSetOf<Long>() // Albums waiting to be processed
    private val takenAlbumIds = mutableSetOf<Long>() // Albums currently being processed
    val albumArts = HashMap<Long, String>()


    /**
     * Adds album ID to processing queue and starts workers if not already running.
     * Uses a producer-consumer pattern with timeout-based shutdown
     */
    fun addAlbumId(albumId: Long) {
        ensureWorkersStarted()
        inputAlbumIds.add(albumId)
    }


    /**
     * Initializes worker coroutines and manager coroutine on first use
     * 7 workers handle parallel processing while manager handles queue management
     */
    private fun ensureWorkersStarted() {
        if(working) return

        repeat(7) { scope.launch {
            while(true) {
                semaphore.acquire() // Wait for available work permit
                val tookId = mutex.withLock {
                    pendingAlbumIds.first().also {
                        pendingAlbumIds.remove(it)
                        takenAlbumIds.add(it)
                    }
                }

                getAlbumArtForId(tookId)
                takenAlbumIds.remove(tookId)
            }
        } }

        // Manager coroutine handles input queue and inactivity timeout
        scope.launch {
            while(awaitsLeft > 0) {
                processInputQueue()
                delay(AWAIT_DELAY)
                awaitsLeft = if(takenAlbumIds.isNotEmpty()) MAX_AWAITS else awaitsLeft - 1
            }

            working = false
            scope.coroutineContext.cancelChildren()
        }

        working = true
    }


    /**
     * Moves album IDs from input queue to pending set while avoiding duplicates
     */
    private suspend fun processInputQueue() {
        while(inputAlbumIds.isNotEmpty()) {
            val tookId = inputAlbumIds.removeFirst()
            mutex.withLock {
                if(isValidForProcessing(tookId)) {
                    pendingAlbumIds.add(tookId)
                    semaphore.release()
                }
            }
            awaitsLeft = MAX_AWAITS + 1
        }
    }


    /**
     * Checks if album ID needs processing
     * Returns true if not already processed, pending, or being processed
     */
    private fun isValidForProcessing(albumId: Long) =
        albumId !in albumArts && albumId !in pendingAlbumIds && albumId !in takenAlbumIds

    private fun getAlbumArtForId(albumId: Long) {
        val albumArt = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId).toString()

        try {
            if (Build.VERSION.SDK_INT < 29) {
                BitmapFactory.decodeFile(albumArt)
            } else {
                contentResolver.loadThumbnail(albumArt.toUri(), Size(20, 20), null)
            }
            albumArts[albumId] = albumArt
        } catch(e: Exception) {
            albumArts[albumId] = defaultImageUri
        }
    }

    companion object {
        // Inactivity check interval
        const val AWAIT_DELAY = 5L
        // Maximum number of empty cycles before shutdown (20 * 5ms = 100ms total timeout)
        const val MAX_AWAITS = 20
    }
}