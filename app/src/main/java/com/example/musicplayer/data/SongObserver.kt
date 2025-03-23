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
import com.example.musicplayer.data.local.entity.Folder
import com.example.musicplayer.data.local.entity.Song
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
class SongObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerRepository: PlayerRepository
) {
    private val contentResolver = context.contentResolver
    private val packageName = context.packageName
    private val scope = CoroutineScope(Dispatchers.IO)
    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) { scope.launch { loadAllSongs() } }
        override fun onChange(selfChange: Boolean, uri: Uri?) { scope.launch { loadAllSongs() } }
        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) { scope.launch { loadAllSongs() } }
        override fun onChange(
            selfChange: Boolean,
            uris: MutableCollection<Uri>,
            flags: Int
        ) { scope.launch { loadAllSongs() } }
    }
    private var isObserving = false

    private val emptyAlbumArts = mutableSetOf<String>()

    private suspend fun loadAllSongs() {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        val currentSongs = playerRepository.getAllSongs().first()
        val newSongs = mutableListOf<Song>()
        val currentFolders = playerRepository.getAllFolders().first()
        var newFolders = mutableSetOf<Folder>()

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

                    newSongs.add(
                        Song(
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
                    newFolders.add(
                        Folder(absoluteName = getFolderAbsoluteName(data), name = getFolderName(data))
                    )
                } while(cursor.moveToNext())
            }
        }

        newFolders = newFolders.map { folder ->
            folder.copy(id = playerRepository.getFolderIdByAbsoluteName(folder.absoluteName))
        }.toMutableSet()

        currentFolders.filter { folder ->
            newFolders.none { it.absoluteName == folder.absoluteName }
        }.forEach { folder ->
            playerRepository.deleteFolder(folder)
        }
        newFolders.forEach { folder ->
            playerRepository.insertFolder(folder)
        }



        val songRowsToDelete = currentSongs.filter { song ->
            newSongs.none { it.uri == song.uri }
        }
        songRowsToDelete.forEach { song ->
            playerRepository.deleteSong(song)
        }
        newSongs.forEach { song ->
            playerRepository.upsertSong(song)
            playerRepository.addSongToFolder(
                songId = song.id,
                folderId = playerRepository.getFolderIdByAbsoluteName(getFolderAbsoluteName(song.data))!!
            )
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

    private fun getFolderAbsoluteName(songPath: String): String {
        return songPath.split('/').dropLast(1).joinToString("/")
    }

    private fun getFolderName(songPath: String): String {
        return songPath.split('/').asReversed()[1]
    }

     fun startObservingSongs() {
        if(isObserving) return
        isObserving = true
        scope.launch {
            loadAllSongs()
        }
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    fun stopObservingSongs() {
        if(!isObserving) return
        contentResolver.unregisterContentObserver(contentObserver)
        isObserving = false
    }
}