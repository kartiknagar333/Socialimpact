package com.example.socialimpact.data.repository

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import com.example.socialimpact.di.qualifier.EmailAuth
import com.example.socialimpact.di.qualifier.GoogleAuth
import com.example.socialimpact.domain.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.content.edit

class AuthRepositoryImpl @Inject constructor(
    @EmailAuth private val emailAuth: FirebaseAuth,
    @GoogleAuth private val googleAuth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences
) : AuthRepository {

    companion object {
        const val KEY_USER_ID = "user_id"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_REG_ID = "reg_id"
        private const val KEY_WEBSITE = "website"
        private const val KEY_INDUSTRY = "industry"
        private const val KEY_PHONE = "phone"
        private const val KEY_LOCATION = "location"
        private const val KEY_BIO = "bio"
    }

    override fun signUpWithEmail(email: String, password: String): Flow<Result<AuthResult>> = flow {
        try {
            val authResult = suspendCancellableCoroutine<AuthResult> { continuation ->
                emailAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        if (exception is FirebaseAuthUserCollisionException) {
                            continuation.resumeWithException(Exception("This email is already registered."))
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
                            continuation.resumeWithException(Exception("No account found with this email."))
                        } else {
                            continuation.resumeWithException(exception)
                        }
                    }
            }
            // Save UID immediately
            sharedPreferences.edit { putString(KEY_USER_ID, authResult.user?.uid) }
            
            // Sync full data
            syncUserDataInternal().collect { }
            
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
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            sharedPreferences.edit { putString(KEY_USER_ID, authResult.user?.uid) }
            if (!isSignup) {
                syncUserDataInternal().collect { }
            }
            emit(Result.success(authResult))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun logout() {
        sharedPreferences.edit { clear() }
        emailAuth.signOut()
        googleAuth.signOut()
    }

    override fun syncUserData(): Flow<Result<Unit>> = syncUserDataInternal()

    @SuppressLint("UseKtx")
    private fun syncUserDataInternal(): Flow<Result<Unit>> = flow {
        try {
            val uid = emailAuth.currentUser?.uid ?: googleAuth.currentUser?.uid 
                ?: throw Exception("No user authenticated")

            val document = FirebaseFirestore.getInstance()
                .collection("account")
                .document(uid)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: throw Exception("Document is empty")
                
                sharedPreferences.edit().apply {
                    putString(KEY_USER_ID, uid) // Ensure UID is saved
                    putString(KEY_USER_TYPE, data["type"] as? String)
                    putString(KEY_FULL_NAME, data["fullName"] as? String)
                    putString(KEY_REG_ID, data["registrationId"] as? String)
                    putString(KEY_WEBSITE, data["website"] as? String)
                    putString(KEY_INDUSTRY, data["industry"] as? String)
                    putString(KEY_PHONE, data["phone"] as? String)
                    putString(KEY_LOCATION, data["location"] as? String)
                    putString(KEY_BIO, data["bio"] as? String)
                }.apply()
                
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("No profile found")))
            }
        } catch (e: Exception) {
            Log.e("Auth", "Sync failed: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
