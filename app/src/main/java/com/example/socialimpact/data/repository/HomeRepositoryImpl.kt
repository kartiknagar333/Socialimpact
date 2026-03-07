package com.example.socialimpact.data.repository

import android.content.SharedPreferences
import android.util.Log
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
    private val sharedPreferences: SharedPreferences
) : HomeRepository {

    companion object {
        private const val TAG = "HomeRepository"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ORG_NAME = "org_name"
        private const val KEY_REG_ID = "reg_id"
        private const val KEY_WEBSITE = "website"
        private const val KEY_INDUSTRY = "industry"
        private const val KEY_PHONE = "phone"
        private const val KEY_LOCATION = "location"
        private const val KEY_BIO = "bio"
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

            // Update local storage
            sharedPreferences.edit().apply {
                putString(KEY_USER_TYPE, type.name)
                putString(KEY_FULL_NAME, fullName)
                putString(KEY_ORG_NAME, organizationName)
                putString(KEY_REG_ID, registrationId)
                putString(KEY_WEBSITE, website)
                putString(KEY_INDUSTRY, industry)
                putString(KEY_PHONE, phone)
                putString(KEY_LOCATION, location)
                putString(KEY_BIO, bio)
            }.apply()
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
            updateLocalProfile(profileData)
            
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

    private fun updateLocalProfile(updates: Map<String, Any>) {
        sharedPreferences.edit().apply {
            updates["type"]?.let { putString(KEY_USER_TYPE, it as String) }
            updates["fullName"]?.let { putString(KEY_FULL_NAME, it as String) }
            updates["organizationName"]?.let { putString(KEY_ORG_NAME, it as String) }
            updates["registrationId"]?.let { putString(KEY_REG_ID, it as String) }
            updates["website"]?.let { putString(KEY_WEBSITE, it as String) }
            updates["industry"]?.let { putString(KEY_INDUSTRY, it as String) }
            updates["phone"]?.let { putString(KEY_PHONE, it as String) }
            updates["location"]?.let { putString(KEY_LOCATION, it as String) }
            updates["bio"]?.let { putString(KEY_BIO, it as String) }
        }.apply()
    }

    override fun getLocalProfile(): LocalProfile? {
        val typeStr = sharedPreferences.getString(KEY_USER_TYPE, null) ?: return null
        val type = try { ProfileType.valueOf(typeStr) } catch (e: Exception) { return null }
        
        return LocalProfile(
            type = type,
            fullName = sharedPreferences.getString(KEY_FULL_NAME, "") ?: "",
            organizationName = sharedPreferences.getString(KEY_ORG_NAME, "") ?: "",
            registrationId = sharedPreferences.getString(KEY_REG_ID, "") ?: "",
            website = sharedPreferences.getString(KEY_WEBSITE, "") ?: "",
            industry = sharedPreferences.getString(KEY_INDUSTRY, "") ?: "",
            phone = sharedPreferences.getString(KEY_PHONE, "") ?: "",
            location = sharedPreferences.getString(KEY_LOCATION, "") ?: "",
            bio = sharedPreferences.getString(KEY_BIO, "") ?: ""
        )
    }

    override fun isProfileSet(): Boolean {
        return sharedPreferences.contains(KEY_USER_TYPE)
    }
}
