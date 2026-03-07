package com.example.socialimpact.domain.repository

import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow

/**
 * Interface for Authentication Repository following Clean Architecture.
 * This abstracts the data source (Firebase) from the domain logic.
 */
interface AuthRepository {
    fun signUpWithEmail(email: String, password: String): Flow<Result<AuthResult>>
    fun signInWithEmail(email: String, password: String): Flow<Result<AuthResult>>
    fun signInWithGoogle(idToken: String, isSignup: Boolean): Flow<Result<AuthResult>>
    fun logout()
    fun syncUserData(): Flow<Result<Unit>>
}
