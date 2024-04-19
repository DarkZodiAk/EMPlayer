package com.example.musicplayer.data

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.data.local.entity.Audio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class AudioPlayer(
    private val player: ExoPlayer
) {

    private var playlist = emptyList<Audio>()

    val isPlayingFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1, replay = 1)
    val currentAudio = MutableSharedFlow<Audio>(extraBufferCapacity = 1, replay = 1)
    val currentTime = MutableSharedFlow<Long>(extraBufferCapacity = 1, replay = 1)

    private val scope = CoroutineScope(Dispatchers.Main)

    private val listener = object : Player.Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            scope.launch {
                currentTime.emit(player.currentPosition)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            scope.launch {
                currentAudio.emit(playlist[player.currentMediaItemIndex])
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            scope.launch {
                isPlayingFlow.emit(isPlaying)
            }
        }
    }

    private var timeUpdater: Job? = null

    init {
        player.addListener(listener)
    }

    private fun launchTimeUpdater() {
        timeUpdater = scope.launch {
            while(true) {
                currentTime.emit(player.currentPosition)
                delay(100L)
            }
        }
    }

    private fun stopTimeUpdater() {
        timeUpdater?.cancel()
    }

    fun setPlaylist(audio: List<Audio>) {
        playlist = audio
        player.setMediaItems(getMediaItemsFromAudio(audio))
    }

    fun setAudioIndex(index: Int) {
        player.seekTo(index, 0L)
    }

    fun play() {
        if(player.availableCommands.contains(Player.COMMAND_PREPARE)) {
            player.prepare()
        }
        launchTimeUpdater()
        player.play()
    }

    fun pause() {
        stopTimeUpdater()
        player.pause()
    }

    fun next() {
        player.seekToNextMediaItem()
    }

    fun previous() {
        player.seekToPreviousMediaItem()
    }

    fun stop() {
        stopTimeUpdater()
        player.stop()
    }

    private fun getMediaItemsFromAudio(audio: List<Audio>): List<MediaItem> {
        return audio.map {
            MediaItem.fromUri(Uri.parse(it.uri))
        }
    }
}