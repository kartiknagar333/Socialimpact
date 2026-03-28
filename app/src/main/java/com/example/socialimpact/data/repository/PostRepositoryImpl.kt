package com.example.socialimpact.data.repository

import android.util.Log
import com.example.socialimpact.domain.model.Donation
import com.example.socialimpact.domain.model.DonationNeedItem
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.PostRepository
import com.google.firebase.firestore.DocumentReference
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

    override fun getDonations(postId: String): Flow<Result<List<Donation>>> = flow {
        try {
            val snapshot = firestore.collection("posts")
                .document(postId)
                .collection("donations")
                .get()
                .await()

            val donations = snapshot.documents.map { doc ->
                val userRef = doc.get("user_ref") as? DocumentReference
                var userName = "Anonymous"
                var userType = "PERSON"
                
                if (userRef != null) {
                    val userDoc = userRef.get().await()
                    if (userDoc.exists()) {
                        userName = userDoc.getString("fullName") ?: "Anonymous"
                        userType = userDoc.getString("type") ?: "PERSON"
                    }
                }

                // Manual mapping for fields with potential different names in Firestore
                val dynamicNeedList = doc.get("dynamicNeed") as? List<Map<String, Any>>
                val items = dynamicNeedList?.map { item ->
                    DonationNeedItem(
                        name = item["name"] as? String ?: "",
                        quantity = item["quantity"] as? String ?: "",
                        isPending = item["ispending"] as? Boolean ?: true,
                        timestamp = item["timestamp"] as? com.google.firebase.Timestamp
                    )
                } ?: emptyList()

                Donation(
                    id = doc.id,
                    userId = userRef?.id ?: "",
                    userName = userName,
                    userType = userType,
                    dynamicNeed = items,
                    lastDonated = doc.getTimestamp("last_donated"),
                    timestamp = doc.getTimestamp("timestamp")
                )
            }
            emit(Result.success(donations))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
