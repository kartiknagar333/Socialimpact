package com.example.socialimpact.data.repository

import com.example.socialimpact.domain.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Implementation of [AuthRepository] using Firebase Authentication.
 * This class handles threading and ensures callback flows are closed properly to prevent memory leaks.
 */
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override fun signUpWithEmail(email: String, password: String): Flow<Result<AuthResult>> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(task.result!!))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Sign up failed")))
                }
                close()
            }
        awaitClose { /* No cleanup needed for Firebase tasks */ }
    }.flowOn(Dispatchers.IO) // Ensures Firebase operations don't block the Main thread

    override fun signInWithGoogle(idToken: String): Flow<Result<AuthResult>> = callbackFlow {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(task.result!!))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Google sign in failed")))
                }
                close()
            }
        awaitClose { }
    }.flowOn(Dispatchers.IO)
}
