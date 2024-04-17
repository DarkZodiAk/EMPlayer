package com.example.musicplayer.domain

fun parseDuration(duration: Long): String {
    val seconds = (duration / 1000) % 60
    val minutes = (duration / (1000 * 60) % 60)
    val hours = (duration / (1000 * 60 * 60) % 24)

    var result = String.format("%02d:%02d", minutes, seconds)
    if(hours != 0L) {
        result = String.format("%02d:", hours) + result
    }

    return result
}