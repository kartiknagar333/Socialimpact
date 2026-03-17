package com.example.socialimpact.di.component

import com.example.socialimpact.di.scope.ActivityScope
import com.example.socialimpact.ui.activity.PaymentActivity
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface PaymentActivityComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): PaymentActivityComponent
    }

    fun inject(activity: PaymentActivity)
}
