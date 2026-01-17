package com.plezha.achi.shared.data.auth

import com.plezha.achi.shared.data.network.apis.AchievementsApi
import com.plezha.achi.shared.data.network.apis.AuthenticationApi
import com.plezha.achi.shared.data.network.apis.PacksApi
import com.plezha.achi.shared.data.network.apis.UploadApi
import com.plezha.achi.shared.data.network.apis.UsersApi
import com.plezha.achi.shared.data.network.models.UserCreateBody
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthState(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val accessToken: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AuthResult {
    data class Success(val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val authApi: AuthenticationApi,
    private val usersApi: UsersApi,
    private val achievementsApi: AchievementsApi,
    private val packsApi: PacksApi,
    private val uploadApi: UploadApi
) {
    private val settings: Settings = Settings()
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USERNAME = "username"
    }
    
    init {
        // Try to restore session from saved settings
        restoreSession()
    }
    
    private fun restoreSession() {
        val savedToken = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        val savedUsername = settings.getStringOrNull(KEY_USERNAME)
        
        if (savedToken != null && savedUsername != null) {
            // Restore token to APIs
            setTokenOnApis(savedToken)
            _authState.value = AuthState(
                isLoggedIn = true,
                username = savedUsername,
                accessToken = savedToken
            )
        }
    }
    
    private fun setTokenOnApis(token: String) {
        achievementsApi.setAccessToken(token)
        packsApi.setAccessToken(token)
        uploadApi.setAccessToken(token)
    }
    
    private fun clearTokenOnApis() {
        achievementsApi.setAccessToken("")
        packsApi.setAccessToken("")
        uploadApi.setAccessToken("")
    }
    
    suspend fun login(username: String, password: String): AuthResult {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        
        return try {
            val response = authApi.loginTokenPost(username, password)
            
            if (response.success) {
                val token = response.body().accessToken
                
                // Save to settings
                settings.putString(KEY_ACCESS_TOKEN, token)
                settings.putString(KEY_USERNAME, username)
                
                // Set token on APIs
                setTokenOnApis(token)
                
                _authState.value = AuthState(
                    isLoggedIn = true,
                    username = username,
                    accessToken = token
                )
                
                AuthResult.Success(username)
            } else {
                val errorMsg = "Login failed: ${response.response.status}"
                _authState.value = _authState.value.copy(isLoading = false, error = errorMsg)
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            _authState.value = _authState.value.copy(isLoading = false, error = errorMsg)
            AuthResult.Error(errorMsg)
        }
    }
    
    suspend fun register(username: String, password: String, displayName: String): AuthResult {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        
        return try {
            val registerResponse = usersApi.registerUserUsersPost(
                UserCreateBody(
                    username = username,
                    password = password,
                    displayName = displayName
                )
            )
            
            if (registerResponse.success) {
                // Auto-login after successful registration
                login(username, password)
            } else {
                val errorMsg = "Registration failed: ${registerResponse.response.status}"
                _authState.value = _authState.value.copy(isLoading = false, error = errorMsg)
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            _authState.value = _authState.value.copy(isLoading = false, error = errorMsg)
            AuthResult.Error(errorMsg)
        }
    }
    
    fun logout() {
        // Clear saved data
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_USERNAME)
        
        // Clear tokens from APIs
        clearTokenOnApis()
        
        // Reset state
        _authState.value = AuthState()
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
