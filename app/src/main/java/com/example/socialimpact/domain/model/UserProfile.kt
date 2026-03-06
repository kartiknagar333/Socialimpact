package com.example.socialimpact.domain.model

enum class ProfileType { PERSON, NGO, CORPORATION }

sealed class UserProfile {
    abstract val uid: String
    abstract val type: ProfileType
    abstract val phone: String
    abstract val location: String
    abstract val bio: String

    data class Person(
        override val uid: String,
        override val phone: String,
        override val location: String,
        override val bio: String,
        val fullName: String,
        override val type: ProfileType = ProfileType.PERSON
    ) : UserProfile()

    data class Ngo(
        override val uid: String,
        override val phone: String,
        override val location: String,
        override val bio: String,
        val organizationName: String,
        val registrationId: String,
        val website: String,
        override val type: ProfileType = ProfileType.NGO
    ) : UserProfile()

    data class Corporation(
        override val uid: String,
        override val phone: String,
        override val location: String,
        override val bio: String,
        val organizationName: String,
        val registrationId: String,
        val website: String,
        val industry: String,
        override val type: ProfileType = ProfileType.CORPORATION
    ) : UserProfile()
}
