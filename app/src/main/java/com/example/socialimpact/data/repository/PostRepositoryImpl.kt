package com.example.socialimpact.data.repository

import android.util.Log
import com.example.socialimpact.domain.model.Donation
import com.example.socialimpact.domain.model.DonationNeedItem
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.PostRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
            Log.d(TAG, "getDonations: Fetching donations for postId: $postId")
            
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
                    try {
                        val userDoc = userRef.get().await()
                        if (userDoc.exists()) {
                            userName = userDoc.getString("fullName") ?: "Anonymous"
                            userType = userDoc.getString("type") ?: "PERSON"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error resolving user_ref: ${e.message}")
                    }
                }

                @Suppress("UNCHECKED_CAST")
                val dynamicNeedList = doc.get("dynamicNeed") as? List<Map<String, Any>>
                val items = dynamicNeedList?.map { item ->
                    DonationNeedItem(
                        name = item["name"] as? String ?: "",
                        quantity = item["quantity"] as? String ?: "",
                        isPending = item["ispending"] as? Boolean ?: true,
                        timestamp = item["timestamp"] as? Timestamp
                    )
                } ?: emptyList()

                val rootLastDonated = doc.getTimestamp("last_donated")
                val itemTimestamp = items.firstOrNull()?.timestamp

                Donation(
                    id = doc.id,
                    userId = userRef?.id ?: "",
                    userName = userName,
                    userType = userType,
                    dynamicNeed = items,
                    lastDonated = rootLastDonated ?: itemTimestamp,
                )
            }
            emit(Result.success(donations))
        } catch (e: Exception) {
            Log.e(TAG, "getDonations error: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun markDonationItemReceived(
        postId: String,
        donationId: String,
        itemName: String,
        quantity: String
    ): Flow<Result<Unit>> = flow {
        try {
            firestore.runTransaction { transaction ->
                val postRef = firestore.collection("posts").document(postId)
                val donationRef = postRef.collection("donations").document(donationId)

                val postSnapshot = transaction.get(postRef)
                val donationSnapshot = transaction.get(donationRef)

                // 1. Update Donation Document: set ispending to false for the specific item
                val donationDynamicNeed = donationSnapshot.get("dynamicNeed") as? List<Map<String, Any>>
                val updatedDonationNeed = donationDynamicNeed?.map { item ->
                    if (item["name"] == itemName) {
                        item.toMutableMap().apply { this["ispending"] = false }
                    } else {
                        item
                    }
                }
                transaction.update(donationRef, "dynamicNeed", updatedDonationNeed)

                // 2. Update Post Document: increase received count in dynamicNeeds
                val postDynamicNeeds = postSnapshot.get("dynamicNeeds") as? List<Map<String, Any>>
                val updatedPostNeeds = postDynamicNeeds?.map { need ->
                    if (need["name"] == itemName) {
                        val currentReceivedValue = need["received"]
                        // Handle potential Double or String in Firestore
                        val currentReceived = when (currentReceivedValue) {
                            is Number -> currentReceivedValue.toDouble()
                            is String -> currentReceivedValue.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        val donatedQuantity = quantity.toDoubleOrNull() ?: 0.0
                        need.toMutableMap().apply { this["received"] = (currentReceived + donatedQuantity).toString() }
                    } else {
                        need
                    }
                }
                transaction.update(postRef, "dynamicNeeds", updatedPostNeeds)
            }.await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "markDonationItemReceived error: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun observePost(postId: String): Flow<HelpRequestPost?> = callbackFlow {
        val listener = firestore.collection("posts").document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val post = snapshot?.toObject(HelpRequestPost::class.java)
                trySend(post)
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
}
