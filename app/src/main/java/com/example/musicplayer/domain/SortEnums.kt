package com.example.musicplayer.domain

enum class SortType {
    TITLE, ARTIST, ALBUM, DURATION, DATE_ADDED, DATE_MODIFIED
}

enum class SortDirection {
    ASC, DESC;
    fun switch(): SortDirection {
        return when(this) {
            ASC -> DESC
            DESC -> ASC
        }
    }
}