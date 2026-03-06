package com.example.socialimpact.di.module

import com.example.socialimpact.data.repository.HomeRepositoryImpl
import com.example.socialimpact.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        impl: HomeRepositoryImpl
    ): HomeRepository
}
