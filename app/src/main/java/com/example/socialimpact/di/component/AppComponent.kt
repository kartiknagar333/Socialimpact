package com.example.socialimpact.di.component

import com.example.socialimpact.di.module.*
import dagger.Component
import javax.inject.Singleton

/**
 * AppComponent — the ROOT Dagger component.
 */
@Singleton
@Component(
    modules = [
        AppModule::class,
        SubcomponentModule::class,
        NetworkModule::class,
        AnalyticsModule::class,
        FirebaseModule::class,
        AuthModule::class,
        HomeModule::class,
        PostModule::class
    ]
)
interface AppComponent {

    fun mainActivityComponent(): MainActivityComponent.Factory
    
    fun homeActivityComponent(): HomeActivityComponent.Factory
    
    fun uploadActivityComponent(): UploadActivityComponent.Factory

    @Component.Builder
    interface Builder {
        fun appModule(module: AppModule): Builder
        fun build(): AppComponent
    }
}
