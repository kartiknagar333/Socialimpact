package com.example.socialimpact.domain.repository

import com.example.socialimpact.ui.layouts.ProfileType
import kotlinx.coroutines.flow.Flow

data class LocalProfile(
    val type: ProfileType,
    val fullName: String,
    val organizationName: String,
    val registrationId: String,
    val website: String,
    val industry: String,
    val phone: String,
    val location: String,
    val bio: String
)

interface HomeRepository {
    fun saveProfile(
        type: ProfileType,
        fullName: String,
        organizationName: String,
        registrationId: String,
        website: String,
        industry: String,
        phone: String,
        location: String,
        bio: String
    ): Flow<Result<Unit>>

    fun updateProfile(
        uid: String,
        profileData: Map<String, Any>
    ): Flow<Result<Unit>>

    fun getLocalProfile(): LocalProfile?
    
    fun isProfileSet(): Boolean
}
