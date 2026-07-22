package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith

object AppAnimations {
    const val QUICK_MILLIS: Int = 140
    const val STANDARD_MILLIS: Int = 220
    const val ENTRANCE_MILLIS: Int = 300

    fun screenTransform(): ContentTransform =
        (fadeIn(tween(STANDARD_MILLIS)) + slideInVertically(tween(STANDARD_MILLIS)) { it / 14 }) togetherWith
            fadeOut(tween(QUICK_MILLIS))
}
