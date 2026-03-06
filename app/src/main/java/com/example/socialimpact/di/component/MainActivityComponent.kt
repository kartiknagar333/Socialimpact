package com.example.socialimpact.di.component

import com.example.socialimpact.MainActivity
import com.example.socialimpact.di.scope.ActivityScope
import dagger.Subcomponent

/**
 * MainActivityComponent — @ActivityScope subcomponent for MainActivity.
 * Modules like FirebaseModule and AuthModule are now in AppComponent
 * so they are inherited here.
 */
@ActivityScope
@Subcomponent
interface MainActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainActivityComponent
    }

    fun inject(activity: MainActivity)
}
