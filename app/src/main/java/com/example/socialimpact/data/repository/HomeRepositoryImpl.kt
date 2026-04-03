package com.example.socialimpact.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.socialimpact.data.local.PreferenceManager
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.model.UserProfile
import com.example.socialimpact.domain.model.ProfileType
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.domain.repository.LocalProfile
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
        return "profile/${type.lowercase()}/${name.trim()}/$uid"
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

            if (type == ProfileType.NGO || type == ProfileType.CORPORATION) {
                profileData["registrationId"] = registrationId
                profileData["website"] = website
                if (type == ProfileType.CORPORATION) {
                    profileData["industry"] = industry
                }
            }

            val currentTime = System.currentTimeMillis()

            // 1. Set data at main account document
            firestore.collection("account").document(uid).set(profileData + ("createdAt" to currentTime)).await()
            Log.d(TAG, "saveProfile: Account document created")

            // 2. Handle type-specific path
            val specificPath = getSpecificPath(type.name, fullName, uid)
            Log.d(TAG, "saveProfile: Specific path: $specificPath")
            
            firestore.document(specificPath).set(profileData + ("lastUpdated" to currentTime)).await()
            Log.d(TAG, "saveProfile: Specific document created")

            // Update local storage
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
            Log.d(TAG, "saveProfile: Local storage updated with UID $uid")

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
            val currentUid = firebaseAuth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            val oldProfile = getLocalProfile() ?: throw Exception("Profile not found locally")
            val uid = oldProfile.uid.ifBlank { currentUid }
            val oldSpecificPath = getSpecificPath(oldProfile.type.name, oldProfile.fullName, uid)

            val updatedDataMap = profileData.toMutableMap()
            if (!updatedDataMap.containsKey("uid")) updatedDataMap["uid"] = uid

            // 1. Update main account document
            firestore.collection("account").document(uid).update(profileData).await()

            // 2. Sync local storage
            preferenceManager.updateLocalProfile(updatedDataMap)

            // 3. Update specific path document
            val updatedProfile = getLocalProfile()!!
            val newSpecificPath = getSpecificPath(updatedProfile.type.name, updatedProfile.fullName, uid)

            if (oldSpecificPath != newSpecificPath) {
                Log.d(TAG, "updateProfile: Path changed from $oldSpecificPath to $newSpecificPath")
                firestore.document(oldSpecificPath).delete().await()
                
                val fullData = firestore.collection("account").document(uid).get().await().data ?: throw Exception("Sync error")
                firestore.document(newSpecificPath).set(fullData + ("lastUpdated" to currentTime)).await()
            } else {
                firestore.document(newSpecificPath).update(profileData + ("lastUpdated" to currentTime)).await()
            }

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "updateProfile error: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLocalProfile(): LocalProfile? {
        val profile = preferenceManager.getLocalProfile() ?: return null
        
        // Ensure UID is always present for current user
        if (profile.uid.isBlank()) {
            val currentUid = firebaseAuth.currentUser?.uid
            if (currentUid != null) {
                Log.d(TAG, "getLocalProfile: Fixing missing UID locally with: $currentUid")
                val fixedProfile = profile.copy(uid = currentUid)
                preferenceManager.saveProfileLocally(
                    uid = currentUid,
                    type = fixedProfile.type.name,
                    fullName = fixedProfile.fullName,
                    regId = fixedProfile.registrationId,
                    website = fixedProfile.website,
                    industry = fixedProfile.industry,
                    phone = fixedProfile.phone,
                    location = fixedProfile.location,
                    bio = fixedProfile.bio
                )
                return fixedProfile
            }
        }
        return profile
    }

    override fun isProfileSet(): Boolean = preferenceManager.isProfileSet()

    override fun getHomePosts(): Flow<PagingData<HelpRequestPost>> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { HomePagingSource(firestore, currentUserId) }
        ).flow
    }
}
