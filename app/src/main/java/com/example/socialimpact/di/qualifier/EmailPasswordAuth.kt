/*
 * File: di/qualifier/EmailPasswordAuth.kt
 * Purpose: Custom Dagger qualifier for Email/Password authentication.
 * Why: To distinguish between different Firebase Auth configurations or sources
 *      when multiple ones are injected.
 * Future: Add new qualifiers here if adding more auth providers (e.g., Google, Github).
 */
package com.example.socialimpact.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class EmailPasswordAuth
