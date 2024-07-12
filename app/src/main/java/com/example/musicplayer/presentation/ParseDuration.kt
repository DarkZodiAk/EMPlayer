package com.example.musicplayer.presentation

fun parseDuration(duration: Long): String {
    val seconds = (duration / 1000) % 60
    val minutes = (duration / (1000 * 60) % 60)
    val hours = (duration / (1000 * 60 * 60) % 24)

    var result = String.format("%01d:%02d", minutes, seconds)
    if(hours != 0L) {
        result = String.format("%01d:", hours) + result
    }

    return result
}