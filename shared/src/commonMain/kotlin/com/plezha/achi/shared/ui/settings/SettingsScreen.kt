package com.plezha.achi.shared.ui.settings

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_chevron_right
import achi.shared.generated.resources.settings_title
import achi.shared.generated.resources.settings_language
import achi.shared.generated.resources.settings_language_subtitle
import achi.shared.generated.resources.legal_terms_of_service
import achi.shared.generated.resources.legal_privacy_policy
import achi.shared.generated.resources.legal_tos_content
import achi.shared.generated.resources.legal_privacy_content
import achi.shared.generated.resources.settings_contact_us
import achi.shared.generated.resources.settings_contact_us_subtitle
import achi.shared.generated.resources.settings_contact_us_email
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.isDebug
import com.plezha.achi.shared.ui.common.LegalTextDialog
import com.plezha.achi.shared.ui.common.TitleBar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SettingsScreen(
    onBackClicked: () -> Unit,
    onNavigateToDebugPanel: () -> Unit
) {
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val contactEmail = stringResource(Res.string.settings_contact_us_email)
    
    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(
            text = stringResource(Res.string.settings_title),
            onBackClicked = onBackClicked,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Language row (disabled for now)
            SettingsRow(
                title = stringResource(Res.string.settings_language),
                subtitle = stringResource(Res.string.settings_language_subtitle),
                onClick = { /* No-op for now */ },
                enabled = false
            )
            HorizontalDivider()
            
            // Contact Us
            SettingsRow(
                title = stringResource(Res.string.settings_contact_us),
                subtitle = stringResource(Res.string.settings_contact_us_subtitle),
                onClick = { uriHandler.openUri("mailto:$contactEmail") }
            )
            HorizontalDivider()
            
            // Terms of Service
            SettingsRow(
                title = stringResource(Res.string.legal_terms_of_service),
                onClick = { showTermsDialog = true }
            )
            HorizontalDivider()
            
            // Privacy Policy
            SettingsRow(
                title = stringResource(Res.string.legal_privacy_policy),
                onClick = { showPrivacyDialog = true }
            )
            HorizontalDivider()
            
            // Debug Panel row (only visible in debug builds)
            if (isDebug) {
                SettingsRow(
                    title = "Debug Panel",
                    subtitle = "Host switching, quick login",
                    onClick = onNavigateToDebugPanel
                )
                HorizontalDivider()
            }
        }
    }
    
    // Legal text dialogs
    if (showTermsDialog) {
        LegalTextDialog(
            title = stringResource(Res.string.legal_terms_of_service),
            content = stringResource(Res.string.legal_tos_content),
            onDismiss = { showTermsDialog = false }
        )
    }
    if (showPrivacyDialog) {
        LegalTextDialog(
            title = stringResource(Res.string.legal_privacy_policy),
            content = stringResource(Res.string.legal_privacy_content),
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val contentAlpha = if (enabled) 1f else 0.38f
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
    }
    val subtitleColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
    }
    val iconTint = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor
                )
            }
        }
        Icon(
            imageVector = vectorResource(Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
