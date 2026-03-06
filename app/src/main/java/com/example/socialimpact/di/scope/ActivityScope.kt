package com.example.socialimpact.di.scope

import javax.inject.Scope

/**
 * ActivityScope — lives as long as the Activity.
 *
 * Use this for:
 * - AuthViewModel (dies when MainActivity is destroyed)
 * - AuthViewModelFactory
 * - Any presenter/manager tied to one screen session
 *
 * Do NOT use for:
 * - FirebaseAuth, SharedPreferences → use @Singleton instead
 *
 * To add FragmentScope later → copy this file, rename to FragmentScope
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope
