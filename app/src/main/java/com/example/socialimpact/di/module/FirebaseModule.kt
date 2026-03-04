/*
 * File: di/module/FirebaseModule.kt
 * Purpose: Provides Firebase instances.
 * Why: Centralizes Firebase instantiation and allows different Qualifiers 
 *      to provide tailored instances of the same class (FirebaseAuth).
 * Future: Add GoogleSignIn, PhoneAuth providers here.
 */
package com.example.socialimpact.di.module

import com.example.socialimpact.di.qualifier.EmailPasswordAuth
import com.example.socialimpact.di.qualifier.PasswordlessAuth
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FirebaseModule {

    @Provides
    @Singleton
    @EmailPasswordAuth
    fun provideEmailPasswordAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    @PasswordlessAuth
    fun providePasswordlessAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // TO ADD GOOGLE LOGIN:
    // 1. Add @GoogleAuth qualifier in di/qualifier/
    // 2. Add @Provides fun provideGoogleSignIn(): GoogleSignInClient here
    // 3. Add GoogleLoginUseCase in domain/usecase/
    // 4. Add GoogleAuthSource in data/source/firebase/
}
