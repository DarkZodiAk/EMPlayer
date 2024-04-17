package com.example.musicplayer.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayer.data.local.entity.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioObserver(
    private val context: Context,
    private val playerRepository: PlayerRepository
) {
    private val contentResolver = context.contentResolver

    private suspend fun loadAllAudio() {
        withContext(Dispatchers.IO){

            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val currentAudio = playerRepository.getAllAudio().first()
            val newAudio = mutableListOf<Audio>()


            contentResolver.query(
                collection,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                while(cursor.moveToNext()){
                    try {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        val dateModified = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(context, contentUri)

                        var title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        var duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        var artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)

                        if (title == null) {
                            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                        }

                        if (duration == null) {
                            duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        }

                        if (artistName == null) {
                            artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
                        }

                        if(artistName == "<unknown>") {
                            artistName = "Неизвестный исполнитель"
                        }

                        if(title != null && artistName != null && duration != null){
                            newAudio.add(
                                Audio(id, title, artistName, duration.toLong(), contentUri.toString(), dateModified.toLong())
                            )
                        }

                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val audioRowsToDelete = currentAudio.filter { audio ->
                newAudio.none { it.id == audio.id }
            }
            audioRowsToDelete.forEach { audio ->
                playerRepository.deleteAudio(audio)
            }
            newAudio.forEach { audio ->
                playerRepository.upsertAudio(audio)
            }
        }
    }


    fun observeAudio(): Flow<Boolean> { //Add FileObserver?
        return callbackFlow {
            launch {
                loadAllAudio()
                send(true)
            }
            val contentObserver = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    launch { loadAllAudio() }
                }

                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    launch { loadAllAudio() }
                }

                override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
                    launch { loadAllAudio() }
                }

                override fun onChange(
                    selfChange: Boolean,
                    uris: MutableCollection<Uri>,
                    flags: Int
                ) {
                    launch { loadAllAudio() }
                }
            }

            contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
            )
            awaitClose {
                contentResolver.unregisterContentObserver(contentObserver)
            }
        }
    }
}