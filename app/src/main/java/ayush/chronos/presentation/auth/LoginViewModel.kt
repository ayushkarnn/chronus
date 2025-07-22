package ayush.chronos.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val userId: String?) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun signInWithCredential(idToken: String?) {
        if (idToken == null) {
            _state.value = LoginState.Error("No ID token received")
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _state.value = LoginState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _state.value = LoginState.Success(auth.currentUser?.uid)
                        } else {
                            _state.value =
                                LoginState.Error(task.exception?.message ?: "Unknown error")
                        }
                    }
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun checkLoggedIn() {
        _state.value = if (auth.currentUser != null) {
            LoginState.Success(auth.currentUser?.uid)
        } else {
            LoginState.Idle
        }
    }

    fun logout() {
        auth.signOut()
        _state.value = LoginState.Idle
    }
}
