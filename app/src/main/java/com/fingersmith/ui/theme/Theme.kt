package com.fingersmith.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Scheme = darkColorScheme(
    primary = Cyan300,
    secondary = Mint200,
    tertiary = Amber300,
    background = Blue900
)

@Composable
fun FingerSmithTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Typography, content = content)
}
