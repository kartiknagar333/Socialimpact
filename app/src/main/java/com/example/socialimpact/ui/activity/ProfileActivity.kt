package com.example.socialimpact.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.di.component.SocialImpactApp
import com.example.socialimpact.ui.layouts.ProfileLayout
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.DonationViewModelFactory
import com.example.socialimpact.ui.viewmodel.ProfileViewModel
import com.example.socialimpact.ui.viewmodel.ProfileViewModelFactory
import javax.inject.Inject

class ProfileActivity : ComponentActivity() {

    @Inject
    lateinit var profileViewModelFactory: ProfileViewModelFactory

    @Inject
    lateinit var donationViewModelFactory: DonationViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as SocialImpactApp)
            .appComponent
            .profileActivityComponent()
            .create()
            .inject(this)

        val isMyProfile = intent.getBooleanExtra("myprofile", false)
        val targetUserId = intent.getStringExtra("userId")?: ""
        val targetUserType = intent.getStringExtra("myusertype")
        val usernamer = intent.getStringExtra("username")

        enableEdgeToEdge()
        
        setContent {
            SocialimpactTheme {
                val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)
                val uiState by profileViewModel.uiState.collectAsState()


                LaunchedEffect(Unit) {
                    profileViewModel.loadProfileData(targetUserId, targetUserType, usernamer)
                }

                ProfileLayout(
                    profile = uiState.profile,
                    myPosts = uiState.myPosts,
                    isMyProfile = isMyProfile,
                    donationFactory = donationViewModelFactory,
                    onBack = { finish() },
                    onUploadClick = {
                        val intent = Intent(this@ProfileActivity, UploadActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
