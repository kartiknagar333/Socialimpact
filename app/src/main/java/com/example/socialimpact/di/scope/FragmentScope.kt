/*
 * File: di/scope/FragmentScope.kt
 * Purpose: Custom Dagger scope for Fragment-level dependencies.
 * Why: To isolate dependencies that should only exist within a Fragment's lifecycle,
 *      ensuring they are recreated if the Fragment is recreated.
 * Future: Use this for child components or modules specific to a single Fragment.
 */
package com.example.socialimpact.di.scope

import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FragmentScope
