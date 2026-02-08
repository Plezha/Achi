package com.plezha.achi.shared.ui.common

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_arrow_back
import achi.shared.generated.resources.common_back
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
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
    Row(
        modifier = modifier
            .padding(bottom = 8.dp)
            .minimumInteractiveComponentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClicked != null) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.common_back)
                )
            }
        } else if (actions != null) {
            // Balance the actions side so the title stays visually centered
            Spacer(Modifier.minimumInteractiveComponentSize())
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .basicMarquee()
        )
        if (actions != null) {
            actions()
        } else if (onBackClicked != null) {
            // Balance the back button side so the title stays visually centered
            Spacer(Modifier.minimumInteractiveComponentSize())
        }
    }
}

@Preview()
@Composable
private fun TitleBarPreview() {
    TitleBar(modifier = Modifier.fillMaxWidth(), text = "Title", onBackClicked = { })
}