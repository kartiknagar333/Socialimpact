package com.example.socialimpact.data.repository

import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PostRepository {

    override fun uploadHelpRequest(post: HelpRequestPost): Flow<Result<Unit>> = flow {
        try {
            val docRef = firestore.collection("posts").document()
            val postWithId = post.copy(id = docRef.id)
            docRef.set(postWithId).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
