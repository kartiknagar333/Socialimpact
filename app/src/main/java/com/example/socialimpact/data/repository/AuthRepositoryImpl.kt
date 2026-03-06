package com.example.socialimpact.data.repository

import android.util.Log
import com.example.socialimpact.di.qualifier.EmailAuth
import com.example.socialimpact.di.qualifier.GoogleAuth
import com.example.socialimpact.domain.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl @Inject constructor(
    @EmailAuth private val emailAuth: FirebaseAuth,
    @GoogleAuth private val googleAuth: FirebaseAuth
) : AuthRepository {

    override fun signUpWithEmail(email: String, password: String): Flow<Result<AuthResult>> = flow {
        try {
            val authResult = suspendCancellableCoroutine<AuthResult> { continuation ->
                emailAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        if (exception is FirebaseAuthUserCollisionException) {
                            continuation.resumeWithException(Exception("This email is already registered. Please sign in instead."))
                        } else {
                            continuation.resumeWithException(exception)
                        }
                    }
            }
            emit(Result.success(authResult))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun signInWithEmail(email: String, password: String): Flow<Result<AuthResult>> = flow {
        try {
            val authResult = suspendCancellableCoroutine<AuthResult> { continuation ->
                emailAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        if (exception is FirebaseAuthInvalidUserException) {
                            continuation.resumeWithException(Exception("No account found with this email. Please sign up."))
                        } else {
                            continuation.resumeWithException(exception)
                        }
                    }
            }
            emit(Result.success(authResult))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun signInWithGoogle(idToken: String, isSignup: Boolean): Flow<Result<AuthResult>> = flow {
        try {
            val authResult = suspendCancellableCoroutine<AuthResult> { continuation ->
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                googleAuth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val isNewUser = result.additionalUserInfo?.isNewUser == true
                        
                        if (isSignup && !isNewUser) {
                            // User tried to SIGN UP but already has an account
                            continuation.resumeWithException(Exception("This Google account is already registered. Please sign in."))
                        } else if (!isSignup && isNewUser) {
                            // User tried to SIGN IN but has no account
                            // Delete the newly created "empty" account to keep database clean
                            result.user?.delete()
                            continuation.resumeWithException(Exception("No account found with this Google email. Please sign up."))
                        } else {
                            continuation.resume(result)
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            Log.d("AUTH_DEBUG", "Google Sign-In Success")
            emit(Result.success(authResult))
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "Google Sign-In Error: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun logout() {
        when {
            isGoogleUser() -> googleAuth.signOut()
            else -> emailAuth.signOut()
        }
    }

    private fun isGoogleUser(): Boolean {
        return emailAuth.currentUser?.providerData?.any {
            it.providerId == GoogleAuthProvider.PROVIDER_ID
        } == true
    }
}
