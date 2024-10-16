package com.example.musicplayer.data

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.MainActivity
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.domain.songPlayer.SongPlayer
import com.example.musicplayer.domain.songPlayer.SongPlayerState
import com.example.musicplayer.domain.usecases.RepeatMode
import com.example.musicplayer.notification.PlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

class SongPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: ExoPlayer
): SongPlayer {
    private var initialPlaylist = emptyList<Song>()
    private var currentPlaylist = emptyList<Song>()
    private var index = 0

    private val scope = CoroutineScope(Dispatchers.Main)
    private var playerState = SongPlayerState()

    private val listener = object : Player.Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            scope.launch {
                updateSongPlayerState(currentTime = player.currentPosition)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            if(error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND){
                scope.launch {
                    updateSongPlayerState(isError = true)
                }
                next()
                scope.launch {
                    updateSongPlayerState(isError = false)
                }
                play()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if(playbackState == Player.STATE_ENDED) {
                onSongEnded()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateSongPlayerState(isPlaying = isPlaying)
        }
    }

    private var timeUpdater: Job? = null

    init {
        player.addListener(listener)
    }

    private fun launchTimeUpdater() {
        timeUpdater = scope.launch {
            while(true) {
                updateSongPlayerState(currentTime = player.currentPosition)
                delay(75L)
            }
        }
    }

    private fun stopTimeUpdater() {
        timeUpdater?.cancel()
    }

    override fun setPlaylist(songs: List<Song>, index: Int) {
        this.index = index
        initialPlaylist = songs
        currentPlaylist = songs
        setMediaItemFromSong(songs[index])
        updateSongPlayerState(isShuffleEnabled = false)
    }

    override fun play() {
        if(player.availableCommands.contains(Player.COMMAND_PREPARE)) {
            player.prepare()
        }
        if(!PlayerService.isServiceActive.value) {
            context.startService(PlayerService.buildStartIntent(context, MainActivity::class.java))
        }
        launchTimeUpdater()
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun next() {
        index = (index + 1) % currentPlaylist.size
        setMediaItemFromSong(currentPlaylist[index])
        play()
    }

    override fun previous() {
        index = (index - 1) % currentPlaylist.size
        if(index < 0) index += currentPlaylist.size
        setMediaItemFromSong(currentPlaylist[index])
        play()
    }

    override fun setPosition(position: Long) {
        player.seekTo(position)
    }

    override fun stop() {
        player.stop()
        stopTimeUpdater()
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        updateSongPlayerState(repeatMode = repeatMode)
    }

    override fun setShuffleMode(enabled: Boolean) {
        if(enabled) {
            val currentSong = currentPlaylist[index]
            currentPlaylist = initialPlaylist.toMutableList().apply {
                removeAt(index)
                shuffle(Random(System.currentTimeMillis()))
                add(0, currentSong)
            }
            index = 0
        } else {
            index = initialPlaylist.indexOf(currentPlaylist[index])
            currentPlaylist = initialPlaylist
        }
        updateSongPlayerState(isShuffleEnabled = enabled)
    }

    private fun onSongEnded() {
        when(playerState.repeatMode) {
            RepeatMode.NO_REPEAT -> {
                if(index == currentPlaylist.size - 1) {
                    pause()
                }
            }
            RepeatMode.REPEAT_ONE -> {
                setPosition(0L)
                play()
            }
            RepeatMode.REPEAT_ALL -> {
                next()
            }
        }
    }

    private fun setMediaItemFromSong(song: Song) {
        val mediaItem = MediaItem.fromUri(Uri.parse(song.uri))
        updateSongPlayerState(currentSong = song)
        player.setMediaItem(mediaItem)
    }

    private fun updateSongPlayerState(
        currentSong: Song? = null,
        isPlaying: Boolean? = null,
        currentTime: Long? = null,
        isError: Boolean? = null,
        repeatMode: RepeatMode? = null,
        isShuffleEnabled: Boolean? = null
    ) {
        playerState = playerState.copy(
            currentSong = currentSong ?: playerState.currentSong,
            isPlaying = isPlaying ?: playerState.isPlaying,
            currentTime = currentTime ?: playerState.currentTime,
            isError = isError ?: playerState.isError,
            repeatMode = repeatMode ?: playerState.repeatMode,
            isShuffleEnabled = isShuffleEnabled ?: playerState.isShuffleEnabled
        )

        SongPlayer.updateState(playerState)
    }
}