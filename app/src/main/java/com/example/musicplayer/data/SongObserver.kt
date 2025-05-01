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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
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
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) { scope.launch { loadAll() } }
        override fun onChange(selfChange: Boolean, uri: Uri?) { scope.launch { loadAll() } }
        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) { scope.launch { loadAll() } }
        override fun onChange(
            selfChange: Boolean,
            uris: MutableCollection<Uri>,
            flags: Int
        ) { scope.launch { loadAll() } }
    }
    private var isObserving = false

    private val emptyAlbumArts = mutableSetOf<String>()

    private suspend fun loadAll() {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        val currentSongs = scope.async { playerRepository.getAllSongs().first() }
        val currentFolders = scope.async { playerRepository.getAllFolders().first() }

        val newSongs = mutableSetOf<Song>()
        var newFolders = mutableSetOf<Folder>()

        contentResolver.query(
            collection,
            null,
            selection,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while(cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                val title = cursor.getString(titleColumn)
                val artistId = cursor.getLong(artistIdColumn)
                val artistName = cursor.getString(artistColumn).let {
                    if (it == "<unknown>") "Неизвестный исполнитель" else it
                }

                val albumId = cursor.getLong(albumIdColumn)
                val albumName = cursor.getString(albumColumn) ?: "Unknown"
                val albumArt = getAlbumArtForId(albumId)

                val duration = cursor.getInt(durationColumn)
                val track = cursor.getInt(trackColumn) % 1000

                val data = cursor.getString(dataColumn)
                val size = cursor.getInt(sizeColumn)
                val dateAdded = cursor.getInt(dateAddedColumn)
                val dateModified = if (dateModifiedColumn != -1) cursor.getInt(dateModifiedColumn) else -1

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
            }
        }

        //Remove deleted songs from DB
        currentSongs.await().filter { song ->
            newSongs.none { it.uri == song.uri }
        }.forEach { song ->
            scope.launch { playerRepository.deleteSong(song) }
        }

        newFolders = newFolders.map { folder ->
            folder.copy(id = playerRepository.getFolderIdByAbsoluteName(folder.absoluteName))
        }.toMutableSet()

        //Remove from DB folders that don't contain any songs, or they were deleted
        currentFolders.await().filter { folder ->
            newFolders.none { it.absoluteName == folder.absoluteName }
        }.forEach { folder ->
            scope.launch { playerRepository.deleteFolder(folder) }
        }

        //Add new folders
        newFolders.forEach { folder ->
            scope.launch { playerRepository.insertFolder(folder) }
        }

        //Add new songs
        newSongs.forEach { song ->
            scope.launch {
                playerRepository.upsertSong(song)
                playerRepository.addSongToFolder(
                    songId = song.id,
                    folderId = playerRepository.getFolderIdByAbsoluteName(getFolderAbsoluteName(song.data))!!
                )
            }
        }
    }

    //Get Uri for AlbumArt of song. If it's not found, return default image Uri
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
            loadAll()
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