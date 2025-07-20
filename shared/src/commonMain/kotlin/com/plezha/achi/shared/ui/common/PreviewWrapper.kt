package com.plezha.achi.shared.ui.common

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun PreviewWrapper(content: @Composable () -> Unit) {
//    AchiTheme {  }
    MaterialTheme {
        Surface(modifier = Modifier.background(Color.White), content = content)
    }
}