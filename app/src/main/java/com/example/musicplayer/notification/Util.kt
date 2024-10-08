package com.example.musicplayer.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Size
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.example.musicplayer.R
import com.example.musicplayer.data.local.entity.Song
import java.io.FileNotFoundException

fun getBitmapForSong(context: Context, song: Song): Bitmap {
    return try {
        if (Build.VERSION.SDK_INT < 29) {
            BitmapFactory.decodeFile(song.albumArt)
        } else {
            context.contentResolver.loadThumbnail(song.albumArt.toUri(), Size(400, 400), null)
        }
    } catch (e: FileNotFoundException) {
        getImage(context, R.drawable.music_icon)
    }
}

fun getImage(context: Context, drawableId: Int): Bitmap {
    return ContextCompat.getDrawable(context, drawableId)!!.toBitmap()
}