package com.example.socialimpact.di.module

import com.example.socialimpact.di.component.*
import dagger.Module

/**
 * SubcomponentModule — registers all Activity-level subcomponents.
 */
@Module(subcomponents = [
    MainActivityComponent::class,
    HomeActivityComponent::class,
    UploadActivityComponent::class,
    ProfileActivityComponent::class,
    PaymentActivityComponent::class
])
class SubcomponentModule
