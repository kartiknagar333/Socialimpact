package com.example.socialimpact.domain.repository

import com.example.socialimpact.domain.model.Donation
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.model.NeedItem
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun uploadHelpRequest(post: HelpRequestPost): Flow<Result<Unit>>
    fun getUserPosts(userId: String): Flow<Result<List<HelpRequestPost>>>
    fun getDonations(postId: String): Flow<Result<List<Donation>>>
    fun markDonationItemReceived(postId: String, donationId: String, itemName: String, quantity: String, itemIndex: Int): Flow<Result<Unit>>
    fun observePost(postId: String): Flow<HelpRequestPost?>
    fun submitItemDonation(postId: String, donorUid: String, donorProfilePath: String, itemName: String, quantity: String): Flow<Result<Unit>>

}
