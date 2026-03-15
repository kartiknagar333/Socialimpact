package com.example.socialimpact.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.socialimpact.domain.repository.LocalProfile
import com.example.socialimpact.ui.layouts.ProfileType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_TYPE = "user_type"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_ORG_NAME = "org_name"
        const val KEY_REG_ID = "reg_id"
        const val KEY_WEBSITE = "website"
        const val KEY_INDUSTRY = "industry"
        const val KEY_PHONE = "phone"
        const val KEY_LOCATION = "location"
        const val KEY_BIO = "bio"
    }

    fun saveProfileLocally(
        uid: String?,
        type: String?,
        fullName: String?,
        orgName: String?,
        regId: String?,
        website: String?,
        industry: String?,
        phone: String?,
        location: String?,
        bio: String?
    ) {
        sharedPreferences.edit {
            putString(KEY_USER_ID, uid)
            putString(KEY_USER_TYPE, type)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_ORG_NAME, orgName)
            putString(KEY_REG_ID, regId)
            putString(KEY_WEBSITE, website)
            putString(KEY_INDUSTRY, industry)
            putString(KEY_PHONE, phone)
            putString(KEY_LOCATION, location)
            putString(KEY_BIO, bio)
        }
    }

    fun updateLocalProfile(updates: Map<String, Any>) {
        sharedPreferences.edit {
            updates["uid"]?.let { putString(KEY_USER_ID, it as String) }
            updates["type"]?.let { putString(KEY_USER_TYPE, it as String) }
            updates["fullName"]?.let { putString(KEY_FULL_NAME, it as String) }
            updates["organizationName"]?.let { putString(KEY_ORG_NAME, it as String) }
            updates["registrationId"]?.let { putString(KEY_REG_ID, it as String) }
            updates["website"]?.let { putString(KEY_WEBSITE, it as String) }
            updates["industry"]?.let { putString(KEY_INDUSTRY, it as String) }
            updates["phone"]?.let { putString(KEY_PHONE, it as String) }
            updates["location"]?.let { putString(KEY_LOCATION, it as String) }
            updates["bio"]?.let { putString(KEY_BIO, it as String) }
        }
    }

    fun getLocalProfile(): LocalProfile? {
        val typeStr = sharedPreferences.getString(KEY_USER_TYPE, null) ?: return null
        val type = try { ProfileType.valueOf(typeStr) } catch (e: Exception) { return null }
        
        return LocalProfile(
            uid = sharedPreferences.getString(KEY_USER_ID, "") ?: "",
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

    fun isProfileSet(): Boolean {
        return sharedPreferences.contains(KEY_USER_TYPE)
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }
}
