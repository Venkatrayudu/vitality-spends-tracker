package com.vmeduri.fintrack.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthService {
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }
    
    suspend fun signUp(email: String, password: String): Boolean {
        return try {
            _isLoading.value = true
            _authError.value = null
            
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            _currentUser.value = firebaseAuth.currentUser
            true
        } catch (e: Exception) {
            _authError.value = e.message ?: "Sign up failed"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            _isLoading.value = true
            _authError.value = null
            
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            _currentUser.value = firebaseAuth.currentUser
            true
        } catch (e: Exception) {
            _authError.value = e.message ?: "Sign in failed"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    fun signOut() {
        try {
            firebaseAuth.signOut()
            _currentUser.value = null
            _authError.value = null
        } catch (e: Exception) {
            _authError.value = e.message ?: "Sign out failed"
        }
    }
    
    suspend fun resetPassword(email: String): Boolean {
        return try {
            _isLoading.value = true
            _authError.value = null
            
            firebaseAuth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            _authError.value = e.message ?: "Password reset failed"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null
    
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid
}
