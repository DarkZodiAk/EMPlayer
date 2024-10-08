package com.example.musicplayer.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.compose.runtime.snapshotFlow
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.musicplayer.data.SongPlayer
import com.example.musicplayer.data.NextSongReceiver
import com.example.musicplayer.data.PauseResumeReceiver
import com.example.musicplayer.data.PrevSongReceiver
import com.example.musicplayer.data.StopReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import com.example.musicplayer.R
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn

@AndroidEntryPoint
class PlayerService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val notificationManager by lazy{
        getSystemService<NotificationManager>()!!
    }

    @Inject
    lateinit var player: SongPlayer
    @Inject
    lateinit var session: MediaSession

    private var scope = CoroutineScope(Dispatchers.Main)

    private var playerState = PlayerServiceState()


    private var activityIntent: Intent? = null
    private var pendingActivityIntent: PendingIntent? = null

    private var pauseResumeIntent: Intent? = null
    private var pauseResumePendingIntent: PendingIntent? = null

    private var nextSongIntent: Intent? = null
    private var nextSongPendingIntent: PendingIntent? = null

    private var prevSongIntent: Intent? = null
    private var prevSongPendingIntent: PendingIntent? = null

    private var stopIntent: Intent? = null
    private var stopPendingIntent: PendingIntent? = null

    private val baseNotification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
    

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> {
                val activityClass = intent.getStringExtra(EXTRA_ACTIVITY_CLASS)
                    ?: throw IllegalArgumentException()
                start(Class.forName(activityClass))
            }
            ACTION_PAUSE_RESUME -> {
                if(session.player.isPlaying) player.pause()
                else player.play()
            }
            ACTION_NEXT_SONG -> player.next()
            ACTION_PREV_SONG -> player.previous()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    private fun start(activityClass: Class<*>) {
        if(!_isServiceActive.value){
            _isServiceActive.value = true
            createNotificationChannel()

            activityIntent = Intent(this, activityClass).apply {
                data = "mplayer://player".toUri()
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            pendingActivityIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(activityIntent!!)
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            }

            pauseResumeIntent = Intent(this, PauseResumeReceiver::class.java).apply { action = ACTION_PAUSE_RESUME }
            pauseResumePendingIntent = PendingIntent.getBroadcast(
                this, 1, pauseResumeIntent!!, PendingIntent.FLAG_IMMUTABLE
            )

            nextSongIntent = Intent(this, NextSongReceiver::class.java).apply { action = ACTION_NEXT_SONG }
            nextSongPendingIntent = PendingIntent.getBroadcast(
                this, 2, nextSongIntent!!, PendingIntent.FLAG_IMMUTABLE
            )

            prevSongIntent = Intent(this, PrevSongReceiver::class.java).apply { action = ACTION_PREV_SONG }
            prevSongPendingIntent = PendingIntent.getBroadcast(
                this, 3, prevSongIntent!!, PendingIntent.FLAG_IMMUTABLE
            )

            stopIntent = Intent(this, StopReceiver::class.java).apply { action = ACTION_STOP }
            stopPendingIntent = PendingIntent.getBroadcast(
                this, 4, stopIntent!!, PendingIntent.FLAG_IMMUTABLE
            )

            startForeground(1, buildNotification(false))
            updateNotification()
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(isPlaying: Boolean): Notification {
        val notification = if(playerState.currentSong.uri == "") baseNotification
            else baseNotification.setLargeIcon(getBitmapForSong(this, playerState.currentSong))

        return notification
            .setContentTitle(playerState.currentSong.title)
            .setContentText(playerState.currentSong.artistName)
            .setContentIntent(pendingActivityIntent!!)
            .clearActions()
            .addAction(R.drawable.close_24, "stop", stopPendingIntent!!)
            .addAction(R.drawable.skip_prev_24, "prev", prevSongPendingIntent!!)
            .addAction(
                if(isPlaying) R.drawable.pause_24 else R.drawable.play_arrow_24,
                "pause_play",
                pauseResumePendingIntent!!
            )
            .addAction(R.drawable.skip_next_24, "next", nextSongPendingIntent!!)
            .setStyle(MediaStyleNotificationHelper.MediaStyle(session))
            .build()
    }

    private fun updateNotification() {
        snapshotFlow { player.playerState.isPlaying }
            .onEach {
                playerState = playerState.copy(isPlaying = it)
                notificationManager.notify(1, buildNotification(playerState.isPlaying))
            }.launchIn(scope)
        snapshotFlow { player.playerState.currentSong }
            .filterNotNull()
            .onEach {
                playerState = playerState.copy(currentSong = it)
                notificationManager.notify(1, buildNotification(playerState.isPlaying))
            }.launchIn(scope)
    }

    private fun stop() {
        stopSelf()
        player.pause()
        _isServiceActive.value = false
        scope.cancel()
        scope = CoroutineScope(Dispatchers.Main)
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Playback",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()

        private const val CHANNEL_ID = "player_channel"

        private const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE_RESUME = "ACTION_PAUSE_RESUME"
        const val ACTION_NEXT_SONG = "ACTION_NEXT_SONG"
        const val ACTION_PREV_SONG = "ACTION_PREV_SONG"
        const val ACTION_STOP = "ACTION_STOP"

        private const val EXTRA_ACTIVITY_CLASS = "EXTRA_ACTIVITY_CLASS"

        fun buildStartIntent(context: Context, activityClass: Class<*>): Intent {
            return Intent(context, PlayerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ACTIVITY_CLASS, activityClass.name)
            }
        }

        fun buildActionIntent(context: Context, action: String): Intent {
            return Intent(context, PlayerService::class.java).also {
                it.action = action
            }
        }
    }
}