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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.ui.common.TitleBar
import org.jetbrains.compose.resources.vectorResource
import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_profile_filled
import androidx.compose.ui.text.AnnotatedString

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    ProfileScreen(
        uiState = uiState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onToggleMode = viewModel::toggleRegisterMode,
        onLogin = viewModel::onLogin,
        onRegister = viewModel::onRegister,
        onLogout = viewModel::onLogout,
        onDebugLogin = viewModel::onDebugLogin
    )
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onToggleMode: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onDebugLogin: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isLoading = uiState.authState.isLoading
    
    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(
            text = "Profile",
            modifier = Modifier.fillMaxWidth()
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
                        username = uiState.authState.username ?: "User",
                        onLogout = onLogout,
                        onDebugLogin = onDebugLogin
                    )
                } else {
                    // Login/Register form
                    AuthForm(
                        uiState = uiState,
                        isLoading = isLoading,
                        onUsernameChanged = onUsernameChanged,
                        onPasswordChanged = onPasswordChanged,
                        onDisplayNameChanged = onDisplayNameChanged,
                        onToggleMode = onToggleMode,
                        onLogin = onLogin,
                        onRegister = onRegister,
                        onDebugLogin = onDebugLogin
                    )
                }
            }
        }
    }
}

@Composable
private fun LoggedInContent(
    username: String,
    onLogout: () -> Unit,
    onDebugLogin: () -> Unit
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
            text = "Logged in",
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
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Username",
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
            Text("Logout")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Debug section
        DebugSection(onDebugLogin = onDebugLogin)
    }
}

@Composable
private fun AuthForm(
    uiState: ProfileUiState,
    isLoading: Boolean,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onToggleMode: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onDebugLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Guest",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (uiState.isRegisterMode) "Create an account" else "Sign in to your account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Form fields
        OutlinedTextField(
            value = uiState.usernameInput,
            onValueChange = onUsernameChanged,
            label = { Text("Username") },
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
            label = { Text("Password") },
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
                    label = { Text("Display Name (optional)") },
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
                Text(if (uiState.isRegisterMode) "Register" else "Login")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle mode button
        TextButton(onClick = onToggleMode) {
            Text(
                if (uiState.isRegisterMode) 
                    "Already have an account? Sign in" 
                else 
                    "Don't have an account? Register"
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        DebugSection(onDebugLogin = onDebugLogin)
    }
}

@Composable
private fun DebugSection(onDebugLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Debug",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = onDebugLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Quick Login (user1/password1)")
        }
    }
}
