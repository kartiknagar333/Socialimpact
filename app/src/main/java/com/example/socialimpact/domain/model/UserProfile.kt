package com.example.socialimpact.domain.model

enum class ProfileType { PERSON, NGO, CORPORATION }

sealed class UserProfile {
    abstract val uid: String
    abstract val type: ProfileType
    abstract val phone: String
    abstract val location: String
    abstract val bio: String
    abstract val fullName: String

    data class Person(
        override val uid: String,
        override val phone: String,
        override val location: String,
        override val bio: String,
        override val fullName: String,
        override val type: ProfileType = ProfileType.PERSON
    ) : UserProfile()

    data class Ngo(
        override val uid: String,
        override val phone: String,
        override val location: String,
        override val bio: String,
        override val fullName: String,
        val registrationId: String,
        val website: String,
        override val type: ProfileType = ProfileType.NGO
    ) : UserProfile()

    data class Corporation(
        override val uid: String,
        override val phone: String,
        override val location: String,
        override val bio: String,
        override val fullName: String,
        val registrationId: String,
        val website: String,
        val industry: String,
        override val type: ProfileType = ProfileType.CORPORATION
    ) : UserProfile()

    companion object {
        fun fromMap(uid: String, profileData: Map<String, Any>): UserProfile {
            val typeStr = (profileData["type"] as? String)?.uppercase() ?: "PERSON"
            val type = try {
                ProfileType.valueOf(typeStr)
            } catch (e: Exception) {
                ProfileType.PERSON
            }

            val phone = profileData["phone"] as? String ?: ""
            val location = profileData["location"] as? String ?: ""
            val bio = profileData["bio"] as? String ?: ""
            
            // Map either field to fullName for backward compatibility
            val name = (profileData["fullName"] as? String) 
                ?: (profileData["organizationName"] as? String) 
                ?: ""

            return when (type) {
                ProfileType.PERSON -> Person(
                    uid = uid,
                    phone = phone,
                    location = location,
                    bio = bio,
                    fullName = name
                )
                ProfileType.NGO -> Ngo(
                    uid = uid,
                    phone = phone,
                    location = location,
                    bio = bio,
                    fullName = name,
                    registrationId = profileData["registrationId"] as? String ?: "",
                    website = profileData["website"] as? String ?: ""
                )
                ProfileType.CORPORATION -> Corporation(
                    uid = uid,
                    phone = phone,
                    location = location,
                    bio = bio,
                    fullName = name,
                    registrationId = profileData["registrationId"] as? String ?: "",
                    website = profileData["website"] as? String ?: "",
                    industry = profileData["industry"] as? String ?: ""
                )
            }
        }
    }
}
