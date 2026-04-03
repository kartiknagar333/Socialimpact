package com.example.socialimpact.data.repository

import android.util.Log
import com.example.socialimpact.domain.model.Donation
import com.example.socialimpact.domain.model.DonationNeedItem
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.PostRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
                .orderBy("last_donated", Query.Direction.DESCENDING)
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
        quantity: String,
        itemIndex: Int
    ): Flow<Result<Unit>> = flow {
        try {
            firestore.runTransaction { transaction ->
                val postRef = firestore.collection("posts").document(postId)
                val donationRef = postRef.collection("donations").document(donationId)

                val postSnapshot = transaction.get(postRef)
                val donationSnapshot = transaction.get(donationRef)

                // 1. Update Donation Document: set ispending to false for ONLY the item at index
                @Suppress("UNCHECKED_CAST")
                val donationDynamicNeed = donationSnapshot.get("dynamicNeed") as? List<Map<String, Any>>
                val updatedDonationNeed = donationDynamicNeed?.mapIndexed { index, item ->
                    if (index == itemIndex) {
                        item.toMutableMap().apply { this["ispending"] = false }
                    } else {
                        item
                    }
                }
                transaction.update(donationRef, "dynamicNeed", updatedDonationNeed)

                // 2. Update Post Document: update received and pending aggregate counts
                @Suppress("UNCHECKED_CAST")
                val postDynamicNeeds = postSnapshot.get("dynamicNeeds") as? List<Map<String, Any>>
                val updatedPostNeeds = postDynamicNeeds?.map { need ->
                    if (need["name"] == itemName) {
                        val currentReceived = (need["received"]?.toString() ?: "0").toDoubleOrNull() ?: 0.0
                        val currentPending = (need["pending"]?.toString() ?: "0").toDoubleOrNull() ?: 0.0
                        val donatedQuantity = quantity.toDoubleOrNull() ?: 0.0
                        
                        need.toMutableMap().apply { 
                            this["received"] = (currentReceived + donatedQuantity).toString()
                            this["pending"] = (currentPending - donatedQuantity).coerceAtLeast(0.0).toString()
                        }
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

    override fun submitItemDonation(
        postId: String,
        donorUid: String,
        donorProfilePath: String,
        itemName: String,
        quantity: String
    ): Flow<Result<Unit>> = flow {
        try {
            Log.d(TAG, "submitItemDonation: Started. PostId: $postId, DonorUid: $donorUid, Item: $itemName, Qty: $quantity")
            Log.d(TAG, "submitItemDonation: Using donorProfilePath: '$donorProfilePath'")
            
            // Step 1: Verify donor document reference
            val segments = donorProfilePath.split("/").filter { it.isNotBlank() }
            if (segments.size % 2 != 0) {
                Log.e(TAG, "submitItemDonation: Invalid donorProfilePath (odd segments): $donorProfilePath")
                throw Exception("Invalid donor profile path")
            }

            val donorRef = firestore.document(donorProfilePath)
            val now = Timestamp.now()

            firestore.runTransaction { transaction ->
                Log.d(TAG, "submitItemDonation: Transaction started")
                val postRef = firestore.collection("posts").document(postId)
                val donationRef = postRef.collection("donations").document(donorUid)
                
                // PERFORM ALL READS FIRST
                val postSnapshot = transaction.get(postRef)
                val existingDonation = transaction.get(donationRef)
                val donorSnapshot = transaction.get(donorRef)

                if (!postSnapshot.exists()) {
                    throw Exception("Post document does not exist")
                }

                // Step 2: Prepare Post's updated dynamicNeeds array
                @Suppress("UNCHECKED_CAST")
                val dynamicNeeds = postSnapshot.get("dynamicNeeds") as? List<Map<String, Any>> ?: emptyList()
                
                var itemFound = false
                val updatedNeeds = dynamicNeeds.map { need ->
                    if (need["name"] == itemName) {
                        itemFound = true
                        val currentPending = (need["pending"]?.toString() ?: "0").toDoubleOrNull() ?: 0.0
                        val donatedQty = quantity.toDoubleOrNull() ?: 0.0
                        need.toMutableMap().apply {
                            this["pending"] = (currentPending + donatedQty).toString()
                        }
                    } else {
                        need
                    }
                }

                if (!itemFound) {
                    throw Exception("Requested item not found in post")
                }

                // Step 3: Prepare Donation data
                val newDonationItem = mapOf(
                    "name" to itemName,
                    "quantity" to quantity,
                    "ispending" to true,
                    "timestamp" to now
                )

                // PERFORM ALL WRITES LAST
                
                // 1. Update Post document
                transaction.update(postRef, "dynamicNeeds", updatedNeeds)

                // 2. Update/Set Donation document
                if (existingDonation.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val existingNeeds = existingDonation.get("dynamicNeed") as? List<Map<String, Any>> ?: emptyList()
                    val updatedNeedsList = existingNeeds + newDonationItem
                    transaction.update(donationRef, "dynamicNeed", updatedNeedsList)
                    transaction.update(donationRef, "last_donated", now)
                } else {
                    val donationData = mapOf(
                        "user_ref" to donorRef,
                        "last_donated" to now,
                        "dynamicNeed" to listOf(newDonationItem)
                    )
                    transaction.set(donationRef, donationData)
                }

                // 3. Update Donor's profile with postId in 'donations' array
                Log.d(TAG, "submitItemDonation: Updating donor profile donations array for post $postId")
                if (donorSnapshot.exists()) {
                    transaction.update(donorRef, "donations", FieldValue.arrayUnion(postId))
                } else {
                    Log.w(TAG, "submitItemDonation: Donor profile doc did not exist at ${donorRef.path}, creating it.")
                    transaction.set(donorRef, mapOf("donations" to listOf(postId)), com.google.firebase.firestore.SetOptions.merge())
                }

                Log.d(TAG, "submitItemDonation: Transaction block finished successfully")
            }.await()
            
            Log.d(TAG, "submitItemDonation: Transaction committed successfully")
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "submitItemDonation: FAILED. Error: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getDonatedPosts(profilePath: String): Flow<Result<List<HelpRequestPost>>> = flow {
        try {
            Log.d(TAG, "getDonatedPosts: Fetching posts for profile path: '$profilePath'")
            
            val segments = profilePath.split("/").filter { it.isNotBlank() }
            if (segments.size % 2 != 0) {
                Log.e(TAG, "getDonatedPosts: Invalid path (odd segments: ${segments.size}): $profilePath")
                emit(Result.success(emptyList<HelpRequestPost>()))
                return@flow
            }

            // 1. Get the list of post IDs from the user's profile
            val donorDoc = firestore.document(profilePath).get().await()
            
            if (!donorDoc.exists()) {
                Log.w(TAG, "getDonatedPosts: Profile document does not exist at $profilePath")
                emit(Result.success(emptyList<HelpRequestPost>()))
                return@flow
            }

            @Suppress("UNCHECKED_CAST")
            val postIds = donorDoc.get("donations") as? List<String> ?: emptyList()
            Log.d(TAG, "getDonatedPosts: Found ${postIds.size} post IDs in profile: $postIds")
            
            if (postIds.isEmpty()) {
                emit(Result.success(emptyList<HelpRequestPost>()))
                return@flow
            }

            // 2. Fetch all posts that match these IDs
            // Firestore 'whereIn' is limited to 30 items
            val snapshot = firestore.collection("posts")
                .whereIn("id", postIds.take(30)) 
                .get()
                .await()
                
            Log.d(TAG, "getDonatedPosts: Query returned ${snapshot.size()} documents from 'posts' collection")
            
            val posts = snapshot.toObjects(HelpRequestPost::class.java)
                .sortedByDescending { it.timestamp }
                
            Log.d(TAG, "getDonatedPosts: Successfully mapped ${posts.size} posts")
            emit(Result.success(posts))
        } catch (e: Exception) {
            Log.e(TAG, "getDonatedPosts error: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
