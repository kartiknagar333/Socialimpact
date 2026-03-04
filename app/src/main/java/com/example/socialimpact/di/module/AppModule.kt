/*
 * File: di/module/AppModule.kt
 * Purpose: Provides application-level dependencies.
 * Why: To make global objects like Context and SharedPreferences available for injection.
 *      These live in the @Singleton scope of the AppComponent.
 * Future: Add new app-level dependencies here (e.g. database, preference managers).
 */
package com.example.socialimpact.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

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
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
}
