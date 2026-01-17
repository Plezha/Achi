package com.plezha.achi.shared.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.ui.common.TitleBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AddAchievementScreen(
    showMessage: suspend CoroutineScope.(String) -> Unit,
    addAchievementViewModel: AddAchievementsViewModel,
) {
    val uiState by addAchievementViewModel.uiState.collectAsState()
    val isLoading = uiState.isLoading

    LaunchedEffect(Unit) {
        addAchievementViewModel.messageFlow.collectLatest { message ->
            showMessage(message)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                text = "Add Achievements",
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Create Achievement Pack",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create an achievement pack by filling out a form",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = addAchievementViewModel::onAddAchievementManually,
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Create Achievement Pack",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Add with a Code",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add an achievement pack directly using a code",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.asciiCode,
                    onValueChange = addAchievementViewModel::onAsciiCodeChange,
                    label = { Text("Enter Code") },
                    enabled = !isLoading,
                    singleLine = true,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            addAchievementViewModel.onCodeSubmit()
                        },
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator()
        }
    }
}


@Preview
@Composable
fun AddAchievementScreenPreview() {

}