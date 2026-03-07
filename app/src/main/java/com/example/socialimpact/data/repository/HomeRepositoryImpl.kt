package com.example.socialimpact.data.repository

import android.content.SharedPreferences
import android.util.Log
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
            
            // Validate name field based on type
            if (type == ProfileType.PERSON && fullName.isBlank()) throw Exception("Full Name is mandatory")
            if ((type == ProfileType.NGO || type == ProfileType.CORPORATION) && organizationName.isBlank()) {
                val label = if (type == ProfileType.NGO) "NGO Name" else "Company Name"
                throw Exception("$label is mandatory")
            }

            // Store profile locally
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

            val profileData = mutableMapOf<String, Any>(
                "phone" to phone,
                "location" to location,
                "bio" to bio,
                "timestamp" to System.currentTimeMillis()
            )

            when (type) {
                ProfileType.PERSON -> {
                    profileData["fullName"] = fullName
                    profileData["type"] = type.name

                }
                ProfileType.NGO, ProfileType.CORPORATION -> {
                    profileData["organizationName"] = organizationName
                    profileData["registrationId"] = registrationId
                    profileData["website"] = website
                    profileData["type"] = type.name
                    if (type == ProfileType.CORPORATION) {
                        profileData["industry"] = industry
                    }
                }
            }


            FirebaseFirestore.getInstance()
                .collection("account")
                .document(uid)
                .set(profileData)
                .await()

            emit(Result.success(Unit))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateProfile(
        uid: String,
        profileData: Map<String, Any>
    ): Flow<Result<Unit>> = flow {
        try {
            FirebaseFirestore.getInstance()
                .collection("account")
                .document(uid)
                .update(profileData)
                .await()

            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

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
