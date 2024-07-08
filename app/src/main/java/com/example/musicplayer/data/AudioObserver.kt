package com.example.musicplayer.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.net.toUri
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerRepository: PlayerRepository
) {
    private val contentResolver = context.contentResolver
    private val packageName = context.packageName
    private val scope = CoroutineScope(Dispatchers.IO)
    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) { scope.launch { loadAllAudio() } }
        override fun onChange(selfChange: Boolean, uri: Uri?) { scope.launch { loadAllAudio() } }
        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) { scope.launch { loadAllAudio() } }
        override fun onChange(
            selfChange: Boolean,
            uris: MutableCollection<Uri>,
            flags: Int
        ) { scope.launch { loadAllAudio() } }
    }
    private var isObserving = false

    private val emptyAlbumArts = mutableSetOf<String>()

    private suspend fun loadAllAudio() {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        val currentAudio = playerRepository.getAllAudio().first()
        val newAudio = mutableListOf<Audio>()

        contentResolver.query(
            collection,
            null,
            selection,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val title =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val artistId =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID))
                    var artistName =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    if (artistName == "<unknown>") {
                        artistName = "Неизвестный исполнитель"
                    }

                    val albumId =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    var albumName =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    if (albumName == null) {
                        albumName = "Unknown"
                    }
                    val albumArt = getAlbumArtForId(albumId)

                    val duration =
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    var track =
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK))
                    while (track >= 1000) {
                        track -= 1000
                    }

                    val data =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val size =
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                    val dateAdded =
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))
                    var dateModified = -1
                    try {
                        dateModified =
                            cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                    } catch (_: IllegalArgumentException) {
                    }

                    newAudio.add(
                        Audio(
                            id,
                            contentUri.toString(),
                            title,
                            artistId,
                            artistName,
                            albumId,
                            albumName,
                            albumArt,
                            duration.toLong(),
                            track.toLong(),
                            data,
                            size.toLong(),
                            dateAdded.toLong(),
                            dateModified.toLong()
                        )
                    )
                } while(cursor.moveToNext())
            }
        }

        val audioRowsToDelete = currentAudio.filter { audio ->
            newAudio.none { it.uri == audio.uri }
        }
        audioRowsToDelete.forEach { audio ->
            playerRepository.deleteAudio(audio)
        }
        newAudio.forEach { audio ->
            playerRepository.upsertAudio(audio)
        }
    }


    private fun getAlbumArtForId(albumId: Long): String {
        val albumArt = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId).toString()
        if(albumArt in emptyAlbumArts)
            return "android.resource://$packageName/drawable/music_icon"

        return try {
            if (Build.VERSION.SDK_INT < 29) {
                BitmapFactory.decodeFile(albumArt)
            } else {
                contentResolver.loadThumbnail(albumArt.toUri(), Size(20, 20), null)
            }
            albumArt
        } catch (e: FileNotFoundException) {
            emptyAlbumArts.add(albumArt)
            "android.resource://$packageName/drawable/music_icon"
        }
    }

     fun startObservingAudio() {
        if(isObserving) return
        isObserving = true
        scope.launch {
            loadAllAudio()
        }
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    fun stopObservingAudio() {
        if(!isObserving) return
        contentResolver.unregisterContentObserver(contentObserver)
        isObserving = false
    }
}