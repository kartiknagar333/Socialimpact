package com.example.socialimpact.di.module

import com.example.socialimpact.di.component.HomeActivityComponent
import com.example.socialimpact.di.component.MainActivityComponent
import dagger.Module

/**
 * SubcomponentModule — registers all Activity-level subcomponents.
 */
@Module(subcomponents = [
    MainActivityComponent::class,
    HomeActivityComponent::class
])
class SubcomponentModule
