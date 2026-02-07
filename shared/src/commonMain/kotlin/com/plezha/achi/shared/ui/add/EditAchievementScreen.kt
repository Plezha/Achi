package com.plezha.achi.shared.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.plezha.achi.shared.ui.common.TitleBar
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import achi.shared.generated.resources.Res
import achi.shared.generated.resources.*

@Composable
fun EditAchievementScreen(
    viewModel: EditAchievementViewModel,
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    EditAchievementScreen(
        uiState = uiState,
        onBackClicked = onBackClicked,
        onTitleChanged = viewModel::onTitleChanged,
        onShortDescriptionChanged = viewModel::onShortDescriptionChanged,
        onLongDescriptionChanged = viewModel::onLongDescriptionChanged,
        onImageSelected = viewModel::onImageSelected,
        onAddStep = viewModel::onAddStep,
        onRemoveStep = viewModel::onRemoveStep,
        onStepDescriptionChanged = viewModel::onStepDescriptionChanged,
        onStepSubstepsAmountChanged = viewModel::onStepSubstepsAmountChanged,
        onSave = { scope.launch { viewModel.onSave() } },
        onCancel = { scope.launch { viewModel.onCancel() } }
    )
}

@Composable
private fun EditAchievementScreen(
    uiState: EditAchievementUiState,
    onBackClicked: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onShortDescriptionChanged: (String) -> Unit,
    onLongDescriptionChanged: (String) -> Unit,
    onImageSelected: (PlatformFile?) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (Int) -> Unit,
    onStepDescriptionChanged: (Int, String) -> Unit,
    onStepSubstepsAmountChanged: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imagePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file ->
        onImageSelected(file)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(
            text = stringResource(Res.string.edit_achievement_title),
            onBackClicked = onBackClicked,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Title (required)
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                label = { Text(stringResource(Res.string.edit_achievement_title_field)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Short Description (required)
            OutlinedTextField(
                value = uiState.shortDescription,
                onValueChange = onShortDescriptionChanged,
                label = { Text(stringResource(Res.string.edit_achievement_short_desc)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Long Description (optional)
            OutlinedTextField(
                value = uiState.longDescription,
                onValueChange = onLongDescriptionChanged,
                label = { Text(stringResource(Res.string.edit_achievement_long_desc)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Achievement Image Section
            Text(
                text = stringResource(Res.string.edit_achievement_image),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.imageFile != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = uiState.imageFile,
                        contentDescription = stringResource(Res.string.edit_achievement_image_cd),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.common_change_image))
                }
            } else {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_plus),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(Res.string.common_select_image))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Steps Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.edit_achievement_steps_count, uiState.steps.size),
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = onAddStep) {
                    Text(stringResource(Res.string.edit_achievement_add_step))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.steps.isEmpty()) {
                Text(
                    text = stringResource(Res.string.edit_achievement_no_steps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            uiState.steps.forEachIndexed { index, step ->
                StepEditCard(
                    index = index,
                    step = step,
                    onDescriptionChanged = { onStepDescriptionChanged(index, it) },
                    onSubstepsAmountChanged = { onStepSubstepsAmountChanged(index, it) },
                    onRemove = { onRemoveStep(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.common_cancel))
                }
                Button(
                    onClick = onSave,
                    enabled = uiState.canSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.common_save))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StepEditCard(
    index: Int,
    step: EditableStep,
    onDescriptionChanged: (String) -> Unit,
    onSubstepsAmountChanged: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.edit_achievement_step_number, index + 1),
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(Res.string.common_remove))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = step.description,
                onValueChange = onDescriptionChanged,
                label = { Text(stringResource(Res.string.edit_achievement_description)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.edit_achievement_substeps),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = step.substepsAmount.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onSubstepsAmountChanged(it) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.edit_achievement_substeps_range),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
