package com.example.socialimpact.domain.repository

import androidx.paging.PagingData
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.model.ProfileType
import kotlinx.coroutines.flow.Flow

data class LocalProfile(
    val uid: String,
    val type: ProfileType,
    val fullName: String,
    val registrationId: String,
    val website: String,
    val industry: String,
    val phone: String,
    val location: String,
    val bio: String
)

interface HomeRepository {
    fun saveProfile(
        uid: String,
        type: ProfileType,
        fullName: String,
        registrationId: String,
        website: String,
        industry: String,
        phone: String,
        location: String,
        bio: String
    ): Flow<Result<Unit>>

    fun updateProfile(
        profileData: Map<String, Any>
    ): Flow<Result<Unit>>

    fun getLocalProfile(): LocalProfile?
    
    fun getUserId(): String
    
    fun isProfileSet(): Boolean

    fun getHomePosts(): Flow<PagingData<HelpRequestPost>>
}
