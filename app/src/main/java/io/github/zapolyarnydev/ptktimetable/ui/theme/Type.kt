package io.github.zapolyarnydev.ptktimetable.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.zapolyarnydev.ptktimetable.R

val MainFontFamily = FontFamily.SansSerif

val HeadingFontFamily = FontFamily(
    Font(R.font.manrope_wght, FontWeight.Normal),
    Font(R.font.manrope_wght, FontWeight.Medium),
    Font(R.font.manrope_wght, FontWeight.SemiBold),
    Font(R.font.manrope_wght, FontWeight.Bold),
    Font(R.font.manrope_wght, FontWeight.ExtraBold)
)

val PtkTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleSmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 17.sp,
        lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MainFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp
    )
)
