package com.example.socialimpact

import android.app.Application
import com.example.socialimpact.di.component.AppComponent
import com.example.socialimpact.di.component.DaggerAppComponent
import com.example.socialimpact.di.module.AppModule

/**
 * SocialImpactApp — Application class. Entry point of the Dagger graph.
 *
 * AppComponent is created ONCE here and stored for the entire app lifetime.
 * All @Singleton objects (FirebaseAuth, SharedPreferences, AuthRepository)
 * live inside this component.
 *
 * How Activities access it:
 *   (application as SocialImpactApp).appComponent
 *
 * IMPORTANT: Register this in AndroidManifest.xml:
 *   android:name=".SocialImpactApp"
 */
class SocialImpactApp : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        // Build the root Dagger graph — happens once when app starts
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}
