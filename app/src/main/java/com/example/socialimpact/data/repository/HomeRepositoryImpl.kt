package com.example.socialimpact.data.repository

import android.util.Log
import com.example.socialimpact.data.local.PreferenceManager
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
    private val preferenceManager: PreferenceManager
) : HomeRepository {

    companion object {
        private const val TAG = "HomeRepository"
    }

    private fun getSpecificPath(type: String, name: String, uid: String): String {
        return "profile/$type/$name/$uid"
    }

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
            Log.d(TAG, "saveProfile: Starting for UID $uid")
            
            if (type == ProfileType.PERSON && fullName.isBlank()) throw Exception("Full Name is mandatory")
            if ((type == ProfileType.NGO || type == ProfileType.CORPORATION) && organizationName.isBlank()) {
                val label = if (type == ProfileType.NGO) "NGO Name" else "Company Name"
                throw Exception("$label is mandatory")
            }

            // Update local storage via PreferenceManager
            preferenceManager.saveProfileLocally(
                type = type.name,
                fullName = fullName,
                orgName = organizationName,
                regId = registrationId,
                website = website,
                industry = industry,
                phone = phone,
                location = location,
                bio = bio
            )
            Log.d(TAG, "saveProfile: Local storage updated")

            val profileData = mutableMapOf<String, Any>(
                "type" to type.name,
                "phone" to phone,
                "location" to location,
                "bio" to bio
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

            val db = FirebaseFirestore.getInstance()
            val currentTime = System.currentTimeMillis()

            // 1. Set data at main account document with created date
            val accountData = profileData.toMutableMap()
            accountData["createdAt"] = currentTime
            Log.d(TAG, "saveProfile: Saving to account/$uid")
            db.collection("account").document(uid).set(accountData).await()

            // 2. Handle type-specific path with last updated
            val name = if (type == ProfileType.PERSON) fullName else organizationName
            val specificPath = getSpecificPath(type.name.lowercase(), name, uid)
            
            val specificData = profileData.toMutableMap()
            specificData["lastUpdated"] = currentTime
            Log.d(TAG, "saveProfile: Saving to specific path: $specificPath")
            db.document(specificPath).set(specificData).await()

            Log.d(TAG, "saveProfile: Success")
            emit(Result.success(Unit))

        } catch (e: Exception) {
            Log.e(TAG, "saveProfile: Failed - ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateProfile(
        uid: String,
        profileData: Map<String, Any>
    ): Flow<Result<Unit>> = flow {
        try {
            Log.d(TAG, "updateProfile: Starting for UID $uid")
            val db = FirebaseFirestore.getInstance()
            val currentTime = System.currentTimeMillis()
            
            // Get old state before update
            val oldProfile = getLocalProfile() ?: throw Exception("Profile not found locally")
            val oldName = if (oldProfile.type == ProfileType.PERSON) oldProfile.fullName else oldProfile.organizationName
            val oldSpecificPath = getSpecificPath(oldProfile.type.name.lowercase(), oldName, uid)

            // 1. Update main account document
            Log.d(TAG, "updateProfile: Updating account/$uid")
            db.collection("account").document(uid).update(profileData).await()

            // 2. Sync local storage with new values
            preferenceManager.updateLocalProfile(profileData)
            
            // 3. Get new state to calculate new path
            val updatedProfile = getLocalProfile()!!
            val newTypeName = (profileData["type"] as? String ?: updatedProfile.type.name).lowercase()
            val newName = if (newTypeName == "person") {
                profileData["fullName"] as? String ?: updatedProfile.fullName
            } else {
                profileData["organizationName"] as? String ?: updatedProfile.organizationName
            }
            val newSpecificPath = getSpecificPath(newTypeName, newName, uid)

            val obj = UserProfile.fromMap(uid, profileData)

            if (oldSpecificPath != newSpecificPath) {
                // Path changed (Name or Type changed). Delete old and create new.
                Log.d(TAG, "updateProfile: Path changed. Deleting old document at: $oldSpecificPath")
                db.document(oldSpecificPath).delete().await()
                
                // Fetch full data from main account to ensure specific path doc is complete
                val fullData = db.collection("account").document(uid).get().await().data ?: throw Exception("Sync error")
                val specificData = fullData.toMutableMap()
                specificData["lastUpdated"] = currentTime
                
                Log.d(TAG, "updateProfile: Creating new document at: $newSpecificPath")
                db.document(newSpecificPath).set(obj).await()
            } else {
                // Path is the same. Only update lastUpdated in the specific path document.
                Log.d(TAG, "updateProfile: Updating lastUpdated at: $newSpecificPath")
                db.document(newSpecificPath).set(obj).await()
            }

            Log.d(TAG, "updateProfile: Success")
            emit(Result.success(Unit))

        } catch (e: Exception) {
            Log.e(TAG, "updateProfile: Failed - ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLocalProfile(): LocalProfile? {
        return preferenceManager.getLocalProfile()
    }

    override fun isProfileSet(): Boolean {
        return preferenceManager.isProfileSet()
    }
}
