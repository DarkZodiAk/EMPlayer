package com.example.musicplayer.data

import android.content.Context
import android.content.pm.PackageManager

class PermissionObserver(
    private val context: Context
) {
    fun checkPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}