package com.example.musicplayer.data

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.MainActivity
import com.example.musicplayer.data.local.SongPlayerPrefs
import com.example.musicplayer.data.local.dao.PlayerStateDao
import com.example.musicplayer.data.local.dao.SongDao
import com.example.musicplayer.data.local.entity.Song
import com.example.musicplayer.data.local.entity.SongInCurrentPlaylist
import com.example.musicplayer.data.local.entity.SongInInitialPlaylist
import com.example.musicplayer.domain.songPlayer.SongPlayer
import com.example.musicplayer.domain.songPlayer.SongPlayerState
import com.example.musicplayer.domain.usecases.RepeatMode
import com.example.musicplayer.notification.PlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

class SongPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SongPlayerPrefs,
    private val playerStateDao: PlayerStateDao,
    private val songDao: SongDao,
    private val player: ExoPlayer
): SongPlayer {
    private var initialPlaylist = emptyList<Song>()
    private var currentPlaylist = emptyList<Song>()
    private var index = 0

    private val scope = CoroutineScope(Dispatchers.Main)
    private val scopeIO = CoroutineScope(Dispatchers.IO)
    private var playerState = SongPlayerState()

    private val listener = object : Player.Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            updateSongPlayerState(currentPosition = player.currentPosition)
        }

        override fun onPlayerError(error: PlaybackException) {
            if(error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND){
                updateSongPlayerState(isError = true)
                next()
                updateSongPlayerState(isError = false)
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
        tryRestoreAllInfo()
        player.addListener(listener)
    }

    private fun launchTimeUpdater() {
        timeUpdater?.cancel()
        timeUpdater = scope.launch {
            while(true) {
                updateSongPlayerState(currentPosition = player.currentPosition)
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
        savePlaylists()
        saveIndex()
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
        stopTimeUpdater()
    }

    override fun next() {
        index = (index + 1) % currentPlaylist.size
        saveIndex()
        setMediaItemFromSong(currentPlaylist[index])
        play()
    }

    override fun previous() {
        index = (index - 1) % currentPlaylist.size
        if(index < 0) index += currentPlaylist.size
        saveIndex()
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
        saveIndex()
        savePlaylists(saveInitial = false)
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
        currentPosition: Long? = null,
        isError: Boolean? = null,
        repeatMode: RepeatMode? = null,
        isShuffleEnabled: Boolean? = null
    ) {
        playerState = playerState.copy(
            currentSong = currentSong ?: playerState.currentSong,
            isPlaying = isPlaying ?: playerState.isPlaying,
            currentPosition = currentPosition ?: playerState.currentPosition,
            isError = isError ?: playerState.isError,
            repeatMode = repeatMode ?: playerState.repeatMode,
            isShuffleEnabled = isShuffleEnabled ?: playerState.isShuffleEnabled
        )
        SongPlayer.updateState(playerState)
        saveState()
    }


    private fun tryRestoreAllInfo() {
        scopeIO.launch {
            prefs.getPlayerState()?.let { state ->
                val job1 = launch {
                    initialPlaylist = playerStateDao.getSongsFromInitialPlaylist().sortedBy {
                        it.index
                    }.mapNotNull { songDao.getSongById(it.id) }
                }
                val job2 = launch {
                    currentPlaylist = playerStateDao.getSongsFromCurrentPlaylist().sortedBy {
                        it.index
                    }.mapNotNull { songDao.getSongById(it.id) }
                }

                listOf(job1, job2).joinAll()
                scope.launch {
                    index = prefs.getPlaylistIndex()
                    setMediaItemFromSong(currentPlaylist[index])
                    player.prepare()
                    setPosition(state.currentPosition)
                }.join()

                updateSongPlayerState(
                    currentSong = state.currentSong,
                    repeatMode = state.repeatMode,
                    isShuffleEnabled = state.isShuffleEnabled
                )
            }
        }
    }

    private fun saveState() {
        prefs.savePlayerState(playerState)
    }

    private fun saveIndex() {
        prefs.savePlaylistIndex(index)
    }

    private fun savePlaylists(saveInitial: Boolean = true) {
        if(saveInitial) {
            scopeIO.launch {
                playerStateDao.deleteSongsFromInitialPlaylist()
                initialPlaylist.forEachIndexed { index, song ->
                    playerStateDao.addSongToInitialPlaylist(SongInInitialPlaylist(song.id, index))
                }
            }
        }

        scopeIO.launch {
            playerStateDao.deleteSongsFromCurrentPlaylist()
            currentPlaylist.forEachIndexed { index, song ->
                playerStateDao.addSongToCurrentPlaylist(SongInCurrentPlaylist(song.id, index))
            }
        }
    }
}