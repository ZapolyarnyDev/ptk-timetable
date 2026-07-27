package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object AppShapes {
    val pill = RoundedCornerShape(50)
    val small = RoundedCornerShape(14.dp)
    val medium = RoundedCornerShape(22.dp)
    val large = RoundedCornerShape(28.dp)
    val schedule = RoundedCornerShape(24.dp)
    val field = small
    val dialog = large

    val material = Shapes(
        small = small,
        medium = medium,
        large = large,
    )
}
