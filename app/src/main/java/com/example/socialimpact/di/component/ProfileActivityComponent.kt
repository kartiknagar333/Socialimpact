package com.example.socialimpact.di.component

import com.example.socialimpact.di.scope.ActivityScope
import com.example.socialimpact.ui.activity.ProfileActivity
import com.example.socialimpact.ui.viewmodel.DonationViewModelFactory
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface ProfileActivityComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): ProfileActivityComponent
    }

    fun inject(activity: ProfileActivity)

    fun donationViewModelFactory(): DonationViewModelFactory
}
