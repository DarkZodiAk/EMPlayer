package com.example.musicplayer.data

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.MainActivity
import com.example.musicplayer.data.local.entity.Audio
import com.example.musicplayer.domain.AudioPlayerState
import com.example.musicplayer.domain.PlayerEventBus
import com.example.musicplayer.notification.PlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: ExoPlayer
) {
    private var playlist = emptyList<Audio>()
    private var playerState = AudioPlayerState()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val listener = object : Player.Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            scope.launch {
                updateAudioPlayerState(currentTime = player.currentPosition)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            scope.launch {
                updateAudioPlayerState(currentAudio = playlist[player.currentMediaItemIndex])
            }

        }

        override fun onPlayerError(error: PlaybackException) {
            if(error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND){
                scope.launch {
                    updateAudioPlayerState(isError = true)
                }
                next()
                scope.launch {
                    updateAudioPlayerState(isError = false)
                }
                play()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            scope.launch {
                updateAudioPlayerState(isPlaying = isPlaying)
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
                updateAudioPlayerState(currentTime = player.currentPosition)
                delay(75L)
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
        if(!PlayerService.isServiceActive.value) {
            context.startService(PlayerService.buildStartIntent(context, MainActivity::class.java))
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

    private suspend fun updateAudioPlayerState(
        currentAudio: Audio? = null,
        isPlaying: Boolean? = null,
        currentTime: Long? = null,
        isError: Boolean? = null
    ) {
        playerState = playerState.copy(
            currentAudio = currentAudio ?: playerState.currentAudio,
            isPlaying = isPlaying ?: playerState.isPlaying,
            currentTime = currentTime ?: playerState.currentTime,
            isError = isError ?: playerState.isError
        )
        PlayerEventBus.sendState(playerState)
    }
}