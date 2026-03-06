package com.example.socialimpact.di.qualifier

import javax.inject.Qualifier

/**
 * @GoogleAuth — marks the FirebaseAuth instance used for Google Sign-In.
 *
 * Currently both @EmailAuth and @GoogleAuth return FirebaseAuth.getInstance().
 * The qualifier separates them logically so in future you can:
 * - Configure them differently (e.g. different FirebaseApp instances)
 * - Swap one without affecting the other
 *
 * To add Biometric login later:
 * Copy this file → rename to BiometricAuth.kt
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleAuth
