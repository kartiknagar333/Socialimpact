/*
 * File: di/qualifier/PasswordlessAuth.kt
 * Purpose: Custom Dagger qualifier for Passwordless (Email Link) authentication.
 * Why: To allow Dagger to provide a specific configuration or instance of FirebaseAuth
 *      intended for passwordless flows.
 * Future: Add new qualifiers like @GoogleAuth or @PhoneAuth to expand login options.
 */
package com.example.socialimpact.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PasswordlessAuth
