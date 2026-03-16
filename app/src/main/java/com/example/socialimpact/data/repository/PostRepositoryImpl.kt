package com.example.socialimpact.data.repository

import android.util.Log
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

    companion object {
        private const val TAG = "PostRepository"
    }

    override fun uploadHelpRequest(post: HelpRequestPost): Flow<Result<Unit>> = flow {
        try {
            Log.d(TAG, "uploadHelpRequest: Starting upload for user ${post.userId}")
            val docRef = firestore.collection("posts").document()
            val postWithId = post.copy(id = docRef.id)
            docRef.set(postWithId).await()
            Log.d(TAG, "uploadHelpRequest: Successfully uploaded post with ID ${docRef.id}")
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "uploadHelpRequest: Failed to upload post", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getUserPosts(userId: String): Flow<Result<List<HelpRequestPost>>> = flow {
        try {
            Log.d(TAG, "getUserPosts: Fetching posts for userId: $userId")
            
            val snapshot = firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            Log.d(TAG, "getUserPosts: Query returned ${snapshot.size()} documents for userId: $userId")
            
            val posts = snapshot.toObjects(HelpRequestPost::class.java)
                .sortedByDescending { it.timestamp }
                
            Log.d(TAG, "getUserPosts: Successfully mapped ${posts.size} posts for userId: $userId")
            emit(Result.success(posts))
        } catch (e: Exception) {
            Log.e(TAG, "getUserPosts: Error fetching posts for userId: $userId", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
