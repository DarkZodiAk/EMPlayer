package com.example.musicplayer.domain.usecases

import com.example.musicplayer.data.AudioPlayer
import javax.inject.Inject

class SwitchRepeatModeUseCase @Inject constructor(
    private val player: AudioPlayer
) {
    operator fun invoke(currentMode: RepeatMode) {
        player.setRepeatMode(currentMode.switch())
    }
}

enum class RepeatMode {
    NO_REPEAT, REPEAT_ONE, REPEAT_ALL;
    fun switch() : RepeatMode {
        return when(this) {
            NO_REPEAT -> REPEAT_ONE
            REPEAT_ONE -> REPEAT_ALL
            REPEAT_ALL -> NO_REPEAT
        }
    }
}
