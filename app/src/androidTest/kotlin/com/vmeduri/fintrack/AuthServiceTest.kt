package com.vmeduri.fintrack.auth

import org.junit.Test
import org.junit.Assert.*

class AuthServiceTest {

    private val authService = AuthService()

    @Test
    fun testAuthServiceInitialization() {
        assertNotNull(authService.currentUser)
        assertNotNull(authService.authError)
        assertNotNull(authService.isLoading)
    }

    @Test
    fun testIsUserAuthenticatedInitiallyFalse() {
        assertFalse(authService.isUserAuthenticated())
    }

    @Test
    fun testGetCurrentUserIdReturnsNullWhenNotAuthenticated() {
        assertNull(authService.getCurrentUserId())
    }

    @Test
    fun testSignOutWhenNotAuthenticatedDoesNotThrow() {
        authService.signOut()
        assertFalse(authService.isUserAuthenticated())
    }
}
