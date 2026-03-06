package com.example.socialimpact.data.repository

import android.util.Log
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.ui.layouts.ProfileType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : HomeRepository {

    override fun saveProfile(
        type: ProfileType,
        fullName: String,
        organizationName: String,
        registrationId: String,
        website: String,
        industry: String,
        phone: String,
        location: String,
        bio: String
    ): Flow<Result<Unit>> = flow {
        try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("User not logged in")
            
            val profileData = mutableMapOf<String, Any>(
                "type" to type.name,
                "phone" to phone,
                "location" to location,
                "bio" to bio,
                "timestamp" to System.currentTimeMillis()
            )

            when (type) {
                ProfileType.PERSON -> {
                    profileData["fullName"] = fullName
                }
                ProfileType.NGO, ProfileType.CORPORATION -> {
                    profileData["organizationName"] = organizationName
                    profileData["registrationId"] = registrationId
                    profileData["website"] = website
                    if (type == ProfileType.CORPORATION) {
                        profileData["industry"] = industry
                    }
                }
            }

            // Call updateProfile to handle the Firestore logic
            updateProfile(uid, profileData).collect { result ->
                emit(result)
            }

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateProfile(
        uid: String,
        profileData: Map<String, Any>
    ): Flow<Result<Unit>> = flow {
        try {
            val type = profileData["type"] as String
            val collectionPath = when (type) {
                "PERSON" -> "account/person/users"
                "NGO" -> "account/ngo_cor/ngo"
                "CORPORATION" -> "account/ngo_cor/corporation"
                else -> throw Exception("Invalid profile type")
            }

            Log.d("Firestore", "Updating profile at: $collectionPath/$uid")

            FirebaseFirestore.getInstance()
                .collection(collectionPath)
                .document(uid)
                .set(profileData)
                .await()

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating profile: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
