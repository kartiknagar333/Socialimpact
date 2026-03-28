package com.example.socialimpact.domain.model

import com.google.firebase.Timestamp

data class DonationNeedItem(
    val name: String = "",
    val quantity: String = "",
    val isPending: Boolean = true,
    val timestamp: Timestamp? = null
)

data class Donation(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userType: String = "",
    val dynamicNeed: List<DonationNeedItem> = emptyList(),
    val lastDonated: Timestamp? = null,
)
