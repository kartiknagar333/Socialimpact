package com.example.socialimpact.domain.repository

import com.example.socialimpact.domain.model.HelpRequestPost
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun uploadHelpRequest(post: HelpRequestPost): Flow<Result<Unit>>
}
