package com.example.oneframe.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.oneframe.R

val pretendardStyle = FontFamily(
    Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.pretendard_regular, FontWeight.Normal, FontStyle.Normal),
)

// Set of Material typography styles to start with
val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = pretendardStyle,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = pretendardStyle,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = pretendardStyle,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    */
)