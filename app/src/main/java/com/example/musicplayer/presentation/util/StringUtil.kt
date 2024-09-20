package com.example.musicplayer.presentation.util

import com.example.musicplayer.domain.SortType

fun SortType.toText(): String {
    return when(this) {
        SortType.TITLE -> "Заголовок"
        SortType.ARTIST -> "Исполнитель"
        SortType.ALBUM -> "Альбом"
        SortType.DURATION -> "Продолжительность"
        SortType.DATE_ADDED -> "Время добавления"
        SortType.DATE_MODIFIED -> "Время изменения"
    }
}

