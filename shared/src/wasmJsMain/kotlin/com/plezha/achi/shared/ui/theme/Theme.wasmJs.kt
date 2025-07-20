package com.plezha.achi.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun colorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme =
    commonColorScheme(darkTheme)