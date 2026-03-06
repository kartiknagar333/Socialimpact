package com.example.socialimpact.di.module

import dagger.Module

/**
 * NetworkModule — provides Retrofit, OkHttp and API services.
 *
 * Currently empty. Add here when you implement REST API calls.
 *
 * To implement:
 * 1. Add Retrofit + OkHttp dependencies in build.gradle
 * 2. Add this module to AppComponent modules list
 * 3. Uncomment and fill the providers below
 *
 * ─── FUTURE ───────────────────────────────────────────────────────────
 * @Provides @Singleton
 * fun provideOkHttpClient(): OkHttpClient =
 *     OkHttpClient.Builder()
 *         .connectTimeout(30, TimeUnit.SECONDS)
 *         .build()
 *
 * @Provides @Singleton
 * fun provideRetrofit(client: OkHttpClient): Retrofit =
 *     Retrofit.Builder()
 *         .baseUrl("https://api.socialimpact.com/")
 *         .client(client)
 *         .addConverterFactory(GsonConverterFactory.create())
 *         .build()
 *
 * @Provides @Singleton
 * fun provideApiService(retrofit: Retrofit): ApiService =
 *     retrofit.create(ApiService::class.java)
 * ──────────────────────────────────────────────────────────────────────
 */
@Module
class NetworkModule
