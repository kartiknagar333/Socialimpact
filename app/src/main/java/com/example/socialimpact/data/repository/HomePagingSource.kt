package com.example.socialimpact.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.socialimpact.domain.model.HelpRequestPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class HomePagingSource(
    private val db: FirebaseFirestore,
    private val currentUserId: String
) : PagingSource<QuerySnapshot, HelpRequestPost>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, HelpRequestPost>): QuerySnapshot? = null

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, HelpRequestPost> {
        return try {
            val currentPage = params.key ?: db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(params.loadSize.toLong())
                .get()
                .await()

            val lastVisible = currentPage.documents.lastOrNull()
            
            // Filtering logic: "id dont match userid"
            // Note: Firestore doesn't support != for multiple fields easily or at scale with ordering.
            // For Paging 3, we filter in-memory if the dataset is manageable, 
            // or use a separate collection/field if it's production-scale.
            val posts = currentPage.toObjects(HelpRequestPost::class.java)
                .filter { it.userId != currentUserId }

            val nextPage = if (lastVisible != null) {
                db.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(params.loadSize.toLong())
                    .get()
                    .await()
            } else null

            LoadResult.Page(
                data = posts,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
