package com.example.socialimpact.domain.repository

import com.example.socialimpact.domain.model.Donation
import com.example.socialimpact.domain.model.HelpRequestPost
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun uploadHelpRequest(post: HelpRequestPost): Flow<Result<Unit>>
    fun getUserPosts(userId: String): Flow<Result<List<HelpRequestPost>>>
    fun getDonations(postId: String): Flow<Result<List<Donation>>>
}
