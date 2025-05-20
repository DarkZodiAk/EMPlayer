package com.example.musicplayer.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayer.data.local.entity.Folder
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.PlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerRepository: PlayerRepository,
    private val albumArtFetcher: AlbumArtFetcher
) {
    private val contentResolver = context.contentResolver
    private val packageName = context.packageName
    private val defaultImageUri = "android.resource://$packageName/drawable/music_icon"

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


    private suspend fun loadAll() {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        val currentSongsDeferred = scope.async { playerRepository.getAllSongs().first().toSet() }
        val currentFoldersDeferred = scope.async { playerRepository.getAllFolders().first() }

        val newSongs = mutableMapOf<Long, Song>()
        val newFolders = mutableMapOf<String, Folder>()

        contentResolver.query(
            collection,
            projection,
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

                albumArtFetcher.addAlbumId(albumId)

                val duration = cursor.getInt(durationColumn)
                val track = cursor.getInt(trackColumn) % 1000

                val data = cursor.getString(dataColumn)
                val size = cursor.getInt(sizeColumn)
                val dateAdded = cursor.getInt(dateAddedColumn)
                val dateModified = if (dateModifiedColumn != -1) cursor.getInt(dateModifiedColumn) else -1

                val song = Song(
                    id,
                    contentUri.toString(),
                    title,
                    artistId,
                    artistName,
                    albumId,
                    albumName,
                    defaultImageUri,
                    duration.toLong(),
                    track.toLong(),
                    data,
                    size.toLong(),
                    dateAdded.toLong(),
                    dateModified.toLong()
                )
                newSongs[id] = song

                val folderAbsoluteName = getFolderAbsoluteName(data)
                val folderName = getFolderName(data)

                newFolders.getOrPut(folderAbsoluteName, { Folder(absoluteName = folderAbsoluteName, name = folderName) })
            }
        }

        val executor = ConcurrentExecutor(32, 30L, 5)

        val currentSongs = currentSongsDeferred.await()
        val currentFolders = currentFoldersDeferred.await()

        //Launch folder upserts to get their Ids later
        val deferredFolderIds = newFolders.mapValues { (_, folder) ->
            scope.async { playerRepository.upsertFolder(folder) }
        }

        //Remove deleted songs from DB
        currentSongs
            .asSequence()
            .filter { it.id !in newSongs }
            .forEach { song ->
                executor.addTask { playerRepository.deleteSong(song) }
            }

        //Remove from DB folders that don't contain any songs, or they were deleted
        currentFolders
            .asSequence()
            .filter { it.absoluteName !in newFolders }
            .forEach { folder ->
                executor.addTask { playerRepository.deleteFolder(folder) }
            }

        //Add new songs
        newSongs.values
            .asSequence()
            .filter { it !in currentSongs }
            .forEach { song ->
                executor.addTask {
                    playerRepository.upsertSong(song)
                    playerRepository.addSongToFolder(
                        songId = song.id,
                        folderId = deferredFolderIds[getFolderAbsoluteName(song.data)]!!.await()
                    )
                }
            }

        while(albumArtFetcher.working) {
            delay(10L)
        }

        newSongs.values
            .asSequence()
            .filter { it !in currentSongs }
            .map { it.copy(albumArt = albumArtFetcher.albumArts[it.albumId]!!) }
            .forEach { song ->
                executor.addTask { playerRepository.upsertSong(song) }
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