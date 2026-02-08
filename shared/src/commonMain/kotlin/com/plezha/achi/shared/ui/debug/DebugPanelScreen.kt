package com.plezha.achi.shared.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.di.ApiConfig
import com.plezha.achi.shared.ui.common.TitleBar

@Composable
fun DebugPanelScreen(
    onBackClicked: () -> Unit,
    onDebugLogin: () -> Unit,
    onPopulateMockData: () -> Unit = {},
    onPopulateMockDataRu: () -> Unit = {},
    isMockDataLoading: Boolean = false
) {
    val currentBaseUrl by ApiConfig.baseUrlFlow.collectAsState()
    var selectedUrl by remember(currentBaseUrl) { mutableStateOf(currentBaseUrl) }
    var customUrl by remember(currentBaseUrl) {
        val matchesPreset = ApiConfig.presets.any { it.second == currentBaseUrl }
        mutableStateOf(if (matchesPreset) "" else currentBaseUrl)
    }
    var isCustomSelected by remember(currentBaseUrl) {
        mutableStateOf(!ApiConfig.presets.any { it.second == currentBaseUrl })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(
            text = "Debug Panel",
            onBackClicked = onBackClicked,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Current host display
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Host",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentBaseUrl,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Host selection
            Text(
                text = "Select Host",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    ApiConfig.presets.forEachIndexed { index, (label, url) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedUrl == url && !isCustomSelected,
                                    onClick = {
                                        selectedUrl = url
                                        isCustomSelected = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = selectedUrl == url && !isCustomSelected,
                                onClick = null
                            )
                            Column {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = url,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < ApiConfig.presets.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Custom URL option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isCustomSelected,
                                onClick = { isCustomSelected = true },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = isCustomSelected,
                            onClick = null
                        )
                        Text(
                            text = "Custom URL",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Custom URL input
            if (isCustomSelected) {
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = {
                        customUrl = it
                        selectedUrl = it
                    },
                    label = { Text("Custom URL") },
                    placeholder = { Text("http://192.168.1.100:8000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Apply button
            Button(
                onClick = {
                    val urlToApply = if (isCustomSelected) customUrl else selectedUrl
                    if (urlToApply.isNotBlank()) {
                        ApiConfig.setBaseUrl(urlToApply)
                    }
                },
                enabled = if (isCustomSelected) customUrl.isNotBlank() else true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Host")
            }

            HorizontalDivider()

            // Quick Login section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedButton(
                onClick = onDebugLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quick Login (user1/password1)")
            }

            OutlinedButton(
                onClick = onPopulateMockData,
                enabled = !isMockDataLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isMockDataLoading) {
                    Text("Creating mock data...")
                } else {
                    Text("Populate Mock Data — EN")
                }
            }

            OutlinedButton(
                onClick = onPopulateMockDataRu,
                enabled = !isMockDataLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isMockDataLoading) {
                    Text("Создаём данные...")
                } else {
                    Text("Populate Mock Data — RU")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
