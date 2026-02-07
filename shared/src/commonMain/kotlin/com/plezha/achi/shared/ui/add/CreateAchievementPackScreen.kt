package com.plezha.achi.shared.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.plezha.achi.shared.ui.common.PreviewWrapper
import com.plezha.achi.shared.ui.common.TitleBar
import com.plezha.achi.shared.ui.common.UiText
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import achi.shared.generated.resources.Res
import achi.shared.generated.resources.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CreateAchievementPackScreen(
    createAchievementPackViewModel: CreateAchievementPackViewModel,
    onBackClicked: () -> Unit = {},
    onAchievementClick: (Int) -> Unit = {}
) {
    val uiState by createAchievementPackViewModel.uiState.collectAsState()

    CreateAchievementPackScreen(
        uiState = uiState,
        onBackClicked = onBackClicked,
        onPackNameChanged = createAchievementPackViewModel::onPackNameChange,
        onPackDescriptionChanged = createAchievementPackViewModel::onPackDescriptionChange,
        onImageSelected = createAchievementPackViewModel::onImageSelected,
        onAddAchievement = createAchievementPackViewModel::onAddAchievement,
        onRemoveAchievement = createAchievementPackViewModel::onRemoveAchievement,
        onAchievementClick = onAchievementClick,
        onAchievementPackSaved = createAchievementPackViewModel::onSaveAchievementPack
    )
}


@Composable
private fun CreateAchievementPackScreen(
    uiState: CreateAchievementPackUiState,
    onBackClicked: () -> Unit,
    onPackNameChanged: (String) -> Unit,
    onPackDescriptionChanged: (String) -> Unit,
    onImageSelected: (PlatformFile?) -> Unit,
    onAddAchievement: () -> Unit,
    onRemoveAchievement: (AchievementId) -> Unit,
    onAchievementClick: (Int) -> Unit,
    onAchievementPackSaved: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imagePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file ->
        onImageSelected(file)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                text = stringResource(Res.string.create_pack_title),
                onBackClicked = onBackClicked,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Pack Name
                OutlinedTextField(
                    value = uiState.packName,
                    onValueChange = onPackNameChanged,
                    label = { Text(stringResource(Res.string.create_pack_name)) },
                    enabled = !uiState.isLoading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pack Description
                OutlinedTextField(
                    value = uiState.packDescription,
                    onValueChange = onPackDescriptionChanged,
                    label = { Text(stringResource(Res.string.create_pack_description_optional)) },
                    enabled = !uiState.isLoading,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Image Picker Section
                Text(
                    text = stringResource(Res.string.create_pack_preview_image),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (uiState.selectedImageFile != null) {
                    // Show selected image preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = uiState.selectedImageFile,
                            contentDescription = stringResource(Res.string.create_pack_preview_image_cd),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.common_change_image))
                    }
                } else {
                    // Show picker button
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_plus),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(Res.string.create_pack_select_preview_image))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Achievements Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.create_pack_achievements_count, uiState.achievements.size),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = onAddAchievement,
                        enabled = !uiState.isLoading
                    ) {
                        Text(stringResource(Res.string.common_add))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Achievement Preview Cards (clickable to edit)
                uiState.achievements.forEachIndexed { index, achievement ->
                    AchievementPreviewCard(
                        achievement = achievement,
                        enabled = !uiState.isLoading,
                        onClick = { onAchievementClick(index) },
                        onRemove = { onRemoveAchievement(achievement.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.achievements.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.create_pack_no_achievements),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                // Error Message
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage?.asString() ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = onAchievementPackSaved,
                    enabled = uiState.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(Res.string.create_pack_save),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Loading Overlay
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun AchievementPreviewCard(
    achievement: EditableAchievementData,
    enabled: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement image preview or placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.imageFile != null) {
                    AsyncImage(
                        model = achievement.imageFile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_plus),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.size(16.dp))
            
            // Title and description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.title.ifBlank { stringResource(Res.string.create_pack_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (achievement.title.isBlank()) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                if (achievement.shortDescription.isNotBlank()) {
                    Text(
                        text = achievement.shortDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                // Show step count if any
                if (achievement.steps.isNotEmpty()) {
                    Text(
                        text = if (achievement.steps.size == 1) 
                            stringResource(Res.string.create_pack_step_count_one, 1)
                        else 
                            stringResource(Res.string.create_pack_steps_count, achievement.steps.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Remove button
            OutlinedButton(
                onClick = onRemove,
                enabled = enabled,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(Res.string.common_remove))
            }
        }
    }
}


@Composable
@Preview
private fun CreateAchievementPackScreenPreview() {
    PreviewWrapper {
        CreateAchievementPackScreen(
            uiState = CreateAchievementPackUiState(
                packName = "Test Pack",
                achievements = listOf(
                    EditableAchievementData(
                        id = AchievementId("1"),
                        title = "First Achievement",
                        shortDescription = "Do something cool"
                    )
                )
            ),
            onBackClicked = {},
            onPackNameChanged = {},
            onPackDescriptionChanged = {},
            onImageSelected = {},
            onAddAchievement = {},
            onRemoveAchievement = {},
            onAchievementClick = {},
            onAchievementPackSaved = {}
        )
    }
}
