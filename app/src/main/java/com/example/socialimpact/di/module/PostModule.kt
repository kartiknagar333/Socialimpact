package com.example.socialimpact.di.module

import com.example.socialimpact.data.repository.PostRepositoryImpl
import com.example.socialimpact.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class PostModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository
}
