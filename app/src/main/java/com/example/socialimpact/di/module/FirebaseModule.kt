package com.example.socialimpact.di.module

import com.example.socialimpact.di.qualifier.EmailAuth
import com.example.socialimpact.di.qualifier.GoogleAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * FirebaseModule — provides all Firebase related dependencies.
 */
@Module
class FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @EmailAuth
    fun provideEmailPasswordAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    @GoogleAuth
    fun provideGoogleAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()
}
