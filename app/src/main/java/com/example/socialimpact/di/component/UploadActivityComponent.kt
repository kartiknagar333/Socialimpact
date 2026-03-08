package com.example.socialimpact.di.component

import com.example.socialimpact.ui.activity.UploadActivity
import com.example.socialimpact.di.scope.ActivityScope
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface UploadActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): UploadActivityComponent
    }

    fun inject(activity: UploadActivity)
}
