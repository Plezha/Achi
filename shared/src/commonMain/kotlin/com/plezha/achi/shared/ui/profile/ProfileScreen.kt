package com.plezha.achi.shared.ui.profile

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.ui.common.LegalTextDialog
import com.plezha.achi.shared.ui.common.TitleBar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import achi.shared.generated.resources.Res
import achi.shared.generated.resources.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    ProfileScreen(
        uiState = uiState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onTermsAcceptedChanged = viewModel::onTermsAcceptedChanged,
        onToggleMode = viewModel::toggleRegisterMode,
        onLogin = viewModel::onLogin,
        onRegister = viewModel::onRegister,
        onLogout = viewModel::onLogout,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onTermsAcceptedChanged: (Boolean) -> Unit,
    onToggleMode: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isLoading = uiState.authState.isLoading
    
    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(
            text = stringResource(Res.string.profile_title),
            modifier = Modifier.fillMaxWidth(),
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_settings),
                        contentDescription = stringResource(Res.string.profile_settings_cd)
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Avatar
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_profile_filled),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedContent(
                targetState = uiState.authState.isLoggedIn,
                label = "auth_content"
            ) { isLoggedIn ->
                if (isLoggedIn) {
                    // Logged in state
                    LoggedInContent(
                        username = uiState.authState.username ?: stringResource(Res.string.common_user_fallback),
                        onLogout = onLogout
                    )
                } else {
                    // Login/Register form
                    AuthForm(
                        uiState = uiState,
                        isLoading = isLoading,
                        onUsernameChanged = onUsernameChanged,
                        onPasswordChanged = onPasswordChanged,
                        onDisplayNameChanged = onDisplayNameChanged,
                        onTermsAcceptedChanged = onTermsAcceptedChanged,
                        onToggleMode = onToggleMode,
                        onLogin = onLogin,
                        onRegister = onRegister
                    )
                }
            }
        }
    }
}

@Composable
private fun LoggedInContent(
    username: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = username,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(Res.string.profile_logged_in),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // User info card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.profile_account),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.profile_username),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = username)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(stringResource(Res.string.profile_logout))
        }
    }
}

@Composable
private fun AuthForm(
    uiState: ProfileUiState,
    isLoading: Boolean,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onTermsAcceptedChanged: (Boolean) -> Unit,
    onToggleMode: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.profile_guest),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (uiState.isRegisterMode) stringResource(Res.string.profile_create_account) else stringResource(Res.string.profile_sign_in),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Form fields
        OutlinedTextField(
            value = uiState.usernameInput,
            onValueChange = onUsernameChanged,
            label = { Text(stringResource(Res.string.profile_username)) },
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.passwordInput,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(Res.string.profile_password)) },
            enabled = !isLoading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (uiState.isRegisterMode) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (!uiState.isRegisterMode) onLogin() }
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Display name field (only for registration)
        AnimatedVisibility(
            visible = uiState.isRegisterMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.displayNameInput,
                    onValueChange = onDisplayNameChanged,
                    label = { Text(stringResource(Res.string.profile_display_name_optional)) },
                    enabled = !isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onRegister() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Terms acceptance checkbox (only for registration)
        AnimatedVisibility(
            visible = uiState.isRegisterMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                TermsAcceptanceRow(
                    checked = uiState.termsAccepted,
                    onCheckedChange = onTermsAcceptedChanged,
                    onTermsClicked = { showTermsDialog = true },
                    onPrivacyClicked = { showPrivacyDialog = true },
                    enabled = !isLoading
                )
            }
        }
        
        // Error message
        AnimatedVisibility(
            visible = uiState.authState.error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = uiState.authState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submit button
        Button(
            onClick = { if (uiState.isRegisterMode) onRegister() else onLogin() },
            enabled = !isLoading && uiState.usernameInput.isNotBlank() && uiState.passwordInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (uiState.isRegisterMode) stringResource(Res.string.profile_register) else stringResource(Res.string.profile_login))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle mode button
        TextButton(onClick = onToggleMode) {
            Text(
                if (uiState.isRegisterMode) 
                    stringResource(Res.string.profile_toggle_to_signin) 
                else 
                    stringResource(Res.string.profile_toggle_to_register)
            )
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
private fun TermsAcceptanceRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    enabled: Boolean
) {
    val prefix = stringResource(Res.string.profile_accept_terms_prefix)
    val termsLabel = stringResource(Res.string.legal_terms_of_service)
    val and = stringResource(Res.string.profile_accept_terms_and)
    val privacyLabel = stringResource(Res.string.legal_privacy_policy)
    val linkColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val textStyle = MaterialTheme.typography.bodySmall.copy(color = textColor)
    
    val annotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = textColor)) {
            append(prefix)
        }
        pushStringAnnotation(tag = "TERMS", annotation = "terms")
        withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
            append(termsLabel)
        }
        pop()
        withStyle(SpanStyle(color = textColor)) {
            append(and)
        }
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
            append(privacyLabel)
        }
        pop()
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        @Suppress("DEPRECATION")
        ClickableText(
            text = annotatedString,
            style = textStyle,
            modifier = Modifier.weight(1f),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                    .firstOrNull()?.let { onTermsClicked() }
                annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                    .firstOrNull()?.let { onPrivacyClicked() }
            }
        )
    }
}
