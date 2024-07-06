package com.example.musicplayer.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayer.notification.PlayerService

class PauseResumeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if(intent?.action == PlayerService.ACTION_PAUSE_RESUME) {
            context.startService(
                PlayerService.buildActionIntent(context, PlayerService.ACTION_PAUSE_RESUME)
            )
        }
    }
}

class NextSongReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if(intent?.action == PlayerService.ACTION_NEXT_SONG) {
            context.startService(
                PlayerService.buildActionIntent(context, PlayerService.ACTION_NEXT_SONG)
            )
        }
    }
}

class PrevSongReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if(intent?.action == PlayerService.ACTION_PREV_SONG) {
            context.startService(
                PlayerService.buildActionIntent(context, PlayerService.ACTION_PREV_SONG)
            )
        }
    }
}

class StopReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if(intent?.action == PlayerService.ACTION_STOP) {
            context.startService(
                PlayerService.buildActionIntent(context, PlayerService.ACTION_STOP)
            )
        }
    }
}