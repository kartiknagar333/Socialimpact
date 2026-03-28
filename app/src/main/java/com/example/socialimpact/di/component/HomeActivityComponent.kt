package com.example.socialimpact.di.component

import com.example.socialimpact.ui.activity.HomeActivity
import com.example.socialimpact.di.scope.ActivityScope
import com.example.socialimpact.ui.viewmodel.DonationViewModelFactory
import com.example.socialimpact.ui.viewmodel.EditProfileViewModelFactory
import com.example.socialimpact.ui.viewmodel.HomeViewModelFactory
import dagger.Subcomponent

/**
 * HomeActivityComponent — @ActivityScope subcomponent for HomeActivity.
 */
@ActivityScope
@Subcomponent
interface HomeActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): HomeActivityComponent
    }

    fun inject(activity: HomeActivity)
    
    fun editProfileViewModelFactory(): EditProfileViewModelFactory
    
    fun homeViewModelFactory(): HomeViewModelFactory

    fun donationViewModelFactory(): DonationViewModelFactory
}
