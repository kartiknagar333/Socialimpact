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
import com.example.socialimpact.ui.viewmodel.ProfileViewModel
import com.example.socialimpact.ui.viewmodel.ProfileViewModelFactory
import javax.inject.Inject

class ProfileActivity : ComponentActivity() {

    @Inject
    lateinit var profileViewModelFactory: ProfileViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as SocialImpactApp)
            .appComponent
            .profileActivityComponent()
            .create()
            .inject(this)

        val isMyProfile = intent.getBooleanExtra("myprofile", false)
        val targetUserId = intent.getStringExtra("userId")

        enableEdgeToEdge()
        
        setContent {
            SocialimpactTheme {
                val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)
                val uiState by profileViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    profileViewModel.loadProfileData(targetUserId)
                }

                ProfileLayout(
                    profile = uiState.profile,
                    myPosts = uiState.myPosts,
                    isMyProfile = isMyProfile,
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
