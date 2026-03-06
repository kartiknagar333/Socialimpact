package com.example.socialimpact.di.scope

import javax.inject.Scope

/**
 * FragmentScope — lives as long as a Fragment.
 *
 * Not used yet. Ready for when you add Fragment-based screens.
 *
 * To use:
 * 1. Create a FragmentComponent (Subcomponent of MainActivityComponent)
 * 2. Annotate Fragment-specific classes with @FragmentScope
 * 3. Register FragmentComponent in SubcomponentModule
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FragmentScope
