package com.example.socialimpact.di.qualifier

import javax.inject.Qualifier

/**
 * @EmailAuth — marks the FirebaseAuth instance used for Email/Password login.
 *
 * Why we need this:
 * FirebaseAuth is provided twice in FirebaseModule (email + google).
 * Dagger needs a label to know which one to inject where.
 *
 * To add a new login method (e.g. Phone OTP):
 * 1. Copy this file → rename to PhoneAuth.kt
 * 2. Add @PhoneAuth FirebaseAuth provider in FirebaseModule
 * 3. Use @PhoneAuth in the class that needs it
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EmailAuth
