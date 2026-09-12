package com.bob.whopaidit.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bob.whopaidit.data.model.User
import com.bob.whopaidit.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val savedEmail: StateFlow<String> = repository.userPreferences.savedEmail.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "",
    )

    val savedPassword: StateFlow<String> = repository.userPreferences.savedPassword.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "",
    )

    val rememberMe: StateFlow<Boolean> = repository.userPreferences.rememberMe.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        onSuccess: (User) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            val errorMsg = "Please fill in all fields"
            _authState.value = AuthState.Error(errorMsg)
            onError(errorMsg)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = repository.signUp(fullName, email, password)
                _authState.value = AuthState.Success(user)
                onSuccess(user)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Sign up failed"
                _authState.value = AuthState.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun login(
        email: String,
        password: String,
        rememberMe: Boolean = false,
        onSuccess: (User) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        if (email.isBlank() || password.isBlank()) {
            val errorMsg = "Please enter email and password"
            _authState.value = AuthState.Error(errorMsg)
            onError(errorMsg)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.userPreferences.saveRememberMe(email, password, rememberMe)
                val user = repository.login(email, password)
                _authState.value = AuthState.Success(user)
                onSuccess(user)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Login failed"
                _authState.value = AuthState.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        if (email.isBlank()) {
            val errorMsg = "Please enter your email address"
            _authState.value = AuthState.Error(errorMsg)
            onError(errorMsg)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.sendPasswordResetEmail(email)
                _authState.value = AuthState.Idle
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Password reset failed"
                _authState.value = AuthState.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        onSuccess: (User) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = repository.signInWithGoogle(context)
                _authState.value = AuthState.Success(user)
                onSuccess(user)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Google Sign In failed"
                _authState.value = AuthState.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }
}
