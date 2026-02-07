package com.plezha.achi.shared.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.auth.AuthRepository
import com.plezha.achi.shared.data.auth.AuthResult
import com.plezha.achi.shared.data.auth.AuthState
import com.plezha.achi.shared.ui.common.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import achi.shared.generated.resources.*

data class ProfileUiState(
    val authState: AuthState = AuthState(),
    val isRegisterMode: Boolean = false,
    val usernameInput: String = "",
    val passwordInput: String = "",
    val displayNameInput: String = ""
)

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val _messageChannel = Channel<UiText>()
    val messageFlow: Flow<UiText> = _messageChannel.receiveAsFlow()
    
    // Debug credentials
    companion object {
        const val DEBUG_USERNAME = "user1"
        const val DEBUG_PASSWORD = "password1"
        const val DEBUG_DISPLAY_NAME = "Test User"
    }
    
    init {
        // Observe auth state from AuthManager
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.update { it.copy(authState = authState) }
            }
        }
    }
    
    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(usernameInput = username) }
    }
    
    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordInput = password) }
    }
    
    fun onDisplayNameChanged(displayName: String) {
        _uiState.update { it.copy(displayNameInput = displayName) }
    }
    
    fun toggleRegisterMode() {
        _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode) }
        authRepository.clearError()
    }
    
    fun onLogin() {
        val state = _uiState.value
        if (state.usernameInput.isBlank() || state.passwordInput.isBlank()) {
            viewModelScope.launch {
                _messageChannel.send(UiText.Resource(Res.string.msg_enter_credentials))
            }
            return
        }
        
        viewModelScope.launch {
            val result = authRepository.login(state.usernameInput, state.passwordInput)
            when (result) {
                is AuthResult.Success -> {
                    _messageChannel.send(UiText.Resource(Res.string.msg_logged_in_as, result.username))
                    clearInputs()
                }
                is AuthResult.Error -> {
                    _messageChannel.send(UiText.Raw(result.message))
                }
            }
        }
    }
    
    fun onRegister() {
        val state = _uiState.value
        if (state.usernameInput.isBlank() || state.passwordInput.isBlank()) {
            viewModelScope.launch {
                _messageChannel.send(UiText.Resource(Res.string.msg_enter_credentials))
            }
            return
        }
        
        val displayName = state.displayNameInput.ifBlank { state.usernameInput }
        
        viewModelScope.launch {
            val result = authRepository.register(state.usernameInput, state.passwordInput, displayName)
            when (result) {
                is AuthResult.Success -> {
                    _messageChannel.send(UiText.Resource(Res.string.msg_registered_as, result.username))
                    clearInputs()
                }
                is AuthResult.Error -> {
                    _messageChannel.send(UiText.Raw(result.message))
                }
            }
        }
    }
    
    fun onLogout() {
        authRepository.logout()
        viewModelScope.launch {
            _messageChannel.send(UiText.Resource(Res.string.msg_logged_out))
        }
    }
    
    fun onDebugLogin() {
        viewModelScope.launch {
            // First try to register (will fail silently if already exists)
            authRepository.register(DEBUG_USERNAME, DEBUG_PASSWORD, DEBUG_DISPLAY_NAME)
            // Then login
            val result = authRepository.login(DEBUG_USERNAME, DEBUG_PASSWORD)
            when (result) {
                is AuthResult.Success -> {
                    _messageChannel.send(UiText.Raw("[DEBUG] Logged in as ${result.username}"))
                }
                is AuthResult.Error -> {
                    _messageChannel.send(UiText.Raw("[DEBUG] Login failed: ${result.message}"))
                }
            }
        }
    }
    
    private fun clearInputs() {
        _uiState.update { 
            it.copy(
                usernameInput = "",
                passwordInput = "",
                displayNameInput = ""
            )
        }
    }
}
