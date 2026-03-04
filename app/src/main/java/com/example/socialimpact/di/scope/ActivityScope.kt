/*
 * File: di/scope/ActivityScope.kt
 * Purpose: Custom Dagger scope for Activity-level dependencies.
 * Why: To ensure that dependencies like ViewModels or local Activity-specific presenters 
 *      live as long as the Activity and are not shared across the entire application.
 * Future: Use this for any subcomponent that should be tied to an Activity lifecycle.
 */
package com.example.socialimpact.di.scope

import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope
