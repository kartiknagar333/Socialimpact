package com.example.socialimpact.di.module

import com.example.socialimpact.data.repository.AuthRepositoryImpl
import com.example.socialimpact.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 * AuthModule — binds interfaces to their implementations.
 */
@Module
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
