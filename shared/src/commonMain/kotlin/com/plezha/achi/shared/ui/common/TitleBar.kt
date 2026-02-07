package com.plezha.achi.shared.ui.common

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_arrow_back
import achi.shared.generated.resources.common_back
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TitleBar(
    text: String,
    onBackClicked: (() -> Unit)? = null,
    modifier: Modifier,
    actions: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .padding(bottom = 8.dp)
            .minimumInteractiveComponentSize()
    ) {
        if (onBackClicked != null) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterStart),
                onClick = onBackClicked
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.common_back)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Center)
                .basicMarquee()
        )
        if (actions != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                actions()
            }
        }
    }
}

@Preview()
@Composable
private fun TitleBarPreview() {
    TitleBar(modifier = Modifier.fillMaxWidth(), text = "Title", onBackClicked = { })
}