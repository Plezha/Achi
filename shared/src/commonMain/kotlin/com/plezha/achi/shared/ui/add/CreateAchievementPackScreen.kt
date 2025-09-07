package com.plezha.achi.shared.ui.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.ui.common.PreviewWrapper
import com.plezha.achi.shared.ui.common.TitleBar
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CreateAchievementPackScreen(
    createAchievementPackViewModel: CreateAchievementPackViewModel
) {
    val uiState by createAchievementPackViewModel.uiState.collectAsState()

    CreateAchievementPackScreen(
        uiState = uiState,
        onPackNameChanged = createAchievementPackViewModel::onPackNameChange,
        onPackDescriptionChanged = createAchievementPackViewModel::onPackDescriptionChange,
        onAddAchievement = createAchievementPackViewModel::onAddAchievement,
        onAchievementTitleChanged = createAchievementPackViewModel::onAchievementTitleChanged,
        onAchievementDescriptionChanged = createAchievementPackViewModel::onAchievementDescriptionChanged,
        onAchievementPackSaved = createAchievementPackViewModel::onSaveAchievementPack
    )
}


@Composable
private fun CreateAchievementPackScreen(
    uiState: CreateAchievementPackUiState,
    onPackNameChanged: (String) -> Unit,
    onPackDescriptionChanged: (String) -> Unit,
    onAddAchievement: () -> Unit,
    onAchievementTitleChanged: (AchievementId, String) -> Unit,
    onAchievementDescriptionChanged: (AchievementId, String) -> Unit,
    onAchievementPackSaved: () -> Unit
) {
    val scrollState = rememberScrollState()
    val packImageFilePickerLauncher = rememberFilePickerLauncher { file ->
        if (file != null) {
            file
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(
            text = "Create Achievement Pack",
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = uiState.packName,
                onValueChange = onPackNameChanged,
                label = { Text("Pack Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.packDescription,
                onValueChange = onPackDescriptionChanged,
                label = { Text("Pack Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddAchievement) {
                Text("Add Achievement")
            }
            Spacer(modifier = Modifier.height(16.dp))
            uiState.achievements.forEachIndexed { index, achievement ->
                OutlinedTextField(
                    value = achievement.title,
                    onValueChange = { onAchievementTitleChanged(achievement.id, it) },
                    label = { Text("Achievement Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = achievement.shortDescription,
                    onValueChange = { onAchievementDescriptionChanged(achievement.id, it) },
                    label = { Text("Achievement Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onAchievementPackSaved) {
                Text("Save Achievement Pack")
            }
        }
    }
}


@Composable
@Preview
fun A() {
    PreviewWrapper {

    }
}