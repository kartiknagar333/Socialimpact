package com.example.socialimpact.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * AppModule — provides app-wide singleton dependencies.
 *
 * What belongs here:
 * - Application context
 * - SharedPreferences
 * - Room Database (when you add it)
 * - Any global utility that lives as long as the app
 *
 * What does NOT belong here:
 * - FirebaseAuth → goes in FirebaseModule
 * - Retrofit → goes in NetworkModule
 * - ViewModel → goes in ActivityScope
 */
@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application = application

    @Provides
    @Singleton
    fun provideContext(): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences("socialimpact_prefs", Context.MODE_PRIVATE)

    // ─── FUTURE ───────────────────────────────────────────────────────────
    // To add Room Database:
    // @Provides @Singleton
    // fun provideDatabase(context: Context): AppDatabase =
    //     Room.databaseBuilder(context, AppDatabase::class.java, "socialimpact.db").build()
    // ──────────────────────────────────────────────────────────────────────
}
