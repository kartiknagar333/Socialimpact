package com.example.socialimpact.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.socialimpact.di.component.SocialImpactApp
import com.example.socialimpact.ui.layouts.PaymentLayout
import com.example.socialimpact.ui.theme.SocialimpactTheme

class PaymentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as SocialImpactApp)
            .appComponent
            .paymentActivityComponent()
            .create()
            .inject(this)

        enableEdgeToEdge()

        setContent {
            SocialimpactTheme {
                PaymentLayout(
                    onBack = { finish() }
                )
            }
        }
    }
}
