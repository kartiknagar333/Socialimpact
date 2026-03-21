package com.example.socialimpact.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.socialimpact.data.local.PreferenceManager
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.model.UserProfile
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.domain.repository.LocalProfile
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
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferenceManager: PreferenceManager
) : HomeRepository {

    companion object {
        private const val TAG = "HomeRepository"
    }

    private fun getSpecificPath(type: String, name: String, uid: String): String {
        return "profile/$type/${name.trim()}/$uid"
    }

    override fun saveProfile(
        uid: String,
        type: ProfileType,
        fullName: String,
        registrationId: String,
        website: String,
        industry: String,
        phone: String,
        location: String,
        bio: String
    ): Flow<Result<Unit>> = flow {
        try {
            Log.d(TAG, "saveProfile: Starting for UID $uid")
            
            if (fullName.isBlank()) throw Exception("Full Name is mandatory")

            val profileData = mutableMapOf<String, Any>(
                "type" to type.name,
                "phone" to phone,
                "location" to location,
                "bio" to bio,
                "uid" to uid,
                "fullName" to fullName
            )

            when (type) {
                ProfileType.NGO, ProfileType.CORPORATION -> {
                    profileData["registrationId"] = registrationId
                    profileData["website"] = website
                    if (type == ProfileType.CORPORATION) {
                        profileData["industry"] = industry
                    }
                }
                else -> {}
            }

            val currentTime = System.currentTimeMillis()

            // 1. Set data at main account document with created date
            val accountData = profileData.toMutableMap()
            accountData["createdAt"] = currentTime
            
            firestore.collection("account").document(uid).set(accountData).await()
            Log.d(TAG, "saveProfile: Account document created")

            // 2. Handle type-specific path with last updated
            val specificPath = getSpecificPath(type.name.lowercase(), fullName, uid)
            Log.d(TAG, "saveProfile: Specific path: $specificPath")


            val specificData = profileData.toMutableMap()
            specificData["lastUpdated"] = currentTime
            
            firestore.document(specificPath).set(specificData).await()
            Log.d(TAG, "saveProfile: Specific document created")

            // Update local storage via PreferenceManager
            preferenceManager.saveProfileLocally(
                uid = uid,
                type = type.name,
                fullName = fullName,
                regId = registrationId,
                website = website,
                industry = industry,
                phone = phone,
                location = location,
                bio = bio
            )
            Log.d(TAG, "saveProfile: Local storage updated")

            emit(Result.success(Unit))

        } catch (e: Exception) {
            Log.e(TAG, "saveProfile error: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateProfile(
        profileData: Map<String, Any>
    ): Flow<Result<Unit>> = flow {
        try {
            val currentTime = System.currentTimeMillis()
            
            // Get old state before update
            val oldProfile = getLocalProfile() ?: throw Exception("Profile not found locally")
            val uid = oldProfile.uid
            val oldName = oldProfile.fullName
            val oldSpecificPath = getSpecificPath(oldProfile.type.name.lowercase(), oldName, uid)

            // 1. Update main account document
            firestore.collection("account").document(uid).update(profileData).await()

            // 2. Sync local storage with new values
            preferenceManager.updateLocalProfile(profileData)

            // 3. Get new state to calculate new path
            val updatedProfile = getLocalProfile()!!
            val newTypeName = (profileData["type"] as? String ?: updatedProfile.type.name).lowercase()
            val newName = profileData["fullName"] as? String ?: updatedProfile.fullName
            val newSpecificPath = getSpecificPath(newTypeName, newName, uid)

            val obj = UserProfile.fromMap(uid, profileData)

            if (oldSpecificPath != newSpecificPath) {
                // Path changed (Name or Type changed). Delete old and create new.
                Log.d(TAG, "updateProfile: Path changed. Deleting old document at: $oldSpecificPath")
                firestore.document(oldSpecificPath).delete().await()
                
                // Fetch full data from main account to ensure specific path doc is complete
                val fullData = firestore.collection("account").document(uid).get().await().data ?: throw Exception("Sync error")
                val specificData = fullData.toMutableMap()
                specificData["lastUpdated"] = currentTime
                
                firestore.document(newSpecificPath).set(obj).await()
            } else {
                // Path is the same. Only update lastUpdated in the specific path document.
                firestore.document(newSpecificPath).set(obj).await()
            }

            emit(Result.success(Unit))

        } catch (e: Exception) {
            Log.e(TAG, "updateProfile error: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLocalProfile(): LocalProfile? {
        return preferenceManager.getLocalProfile()
    }

    override fun isProfileSet(): Boolean {
        return preferenceManager.isProfileSet()
    }

    override fun getHomePosts(): Flow<PagingData<HelpRequestPost>> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { HomePagingSource(firestore, currentUserId) }
        ).flow
    }
}
