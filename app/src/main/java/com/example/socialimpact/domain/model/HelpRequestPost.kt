package com.example.socialimpact.domain.model

data class NeedItem(
    val name: String = "",
    val unit: String = "Pcs",
    val quantity: String = ""
)

data class HelpRequestPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userType: String = "",
    val title: String = "",
    val description: String = "",
    val selectedNeeds: List<String> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val fundAmount: String = "",
    val dynamicNeeds: List<NeedItem> = emptyList(),
    val startDate: String = "",
    val endDate: String = "",
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
