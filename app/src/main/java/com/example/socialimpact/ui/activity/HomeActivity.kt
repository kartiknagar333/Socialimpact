package com.example.socialimpact.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.socialimpact.di.component.SocialImpactApp
import com.example.socialimpact.ui.layouts.EditProfileLayout
import com.example.socialimpact.ui.layouts.HomeLayout
import com.example.socialimpact.ui.theme.AppTheme
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.AuthViewModel
import com.example.socialimpact.ui.viewmodel.AuthViewModelFactory
import com.example.socialimpact.ui.viewmodel.EditProfileViewModel
import com.example.socialimpact.ui.viewmodel.EditProfileViewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeActivity : ComponentActivity() {

    @Inject
    lateinit var authViewModelFactory: AuthViewModelFactory

    @Inject
    lateinit var editProfileViewModelFactory: EditProfileViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as SocialImpactApp)
            .appComponent
            .homeActivityComponent()
            .create()
            .inject(this)

        val isFromSignup = intent.getBooleanExtra("IS_FROM_SIGNUP", false)

        enableEdgeToEdge()

        setContent {
            var currentTheme by remember { mutableStateOf(AppTheme.SYSTEM) }

            SocialimpactTheme(appTheme = currentTheme) {
                HomeNavigation(
                    authFactory = authViewModelFactory,
                    editProfileFactory = editProfileViewModelFactory,
                    isFromSignupExtra = isFromSignup,
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it },
                    onLogoutTriggered = { navigateToLogin() },
                    onProfileClick = {
                        val intent = Intent(this, ProfileActivity::class.java).apply {
                            putExtra("myprofile", true)
                        }
                        startActivity(intent)
                    },
                    onUploadClick = {
                        val intent = Intent(this, UploadActivity::class.java)
                        startActivity(intent)
                    },
                    onPaymentClick = {
                        val intent = Intent(this, PaymentActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun navigateToLogin() {
        lifecycleScope.launch {
            try {
                val credentialManager = CredentialManager.create(this@HomeActivity)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Error clearing credentials", e)
            } finally {
                val intent = Intent(this@HomeActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }
}

@Composable
fun HomeNavigation(
    authFactory: ViewModelProvider.Factory,
    editProfileFactory: ViewModelProvider.Factory,
    isFromSignupExtra: Boolean,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onLogoutTriggered: () -> Unit,
    onProfileClick: () -> Unit,
    onUploadClick: () -> Unit,
    onPaymentClick: () -> Unit
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = viewModel(factory = authFactory)

    NavHost(
        navController = navController, 
        startDestination = if (isFromSignupExtra) "edit_profile" else "home"
    ) {
        composable("home") {
            HomeLayout(
                onLogout = {
                    coroutineScope.launch {
                        authViewModel.logout()
                        onLogoutTriggered()
                    }
                },
                onEditProfile = {
                    navController.navigate("edit_profile/false")
                },
                onPaymentClick = onPaymentClick,
                onProfileClick = onProfileClick,
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            )
        }
        composable("edit_profile") {
            EditProfileScreen(editProfileFactory, navController, isFromSignupExtra)
        }
        composable("edit_profile/{isFromSignup}") { backStackEntry ->
            val isFromSignup = backStackEntry.arguments?.getString("isFromSignup")?.toBoolean() ?: false
            EditProfileScreen(editProfileFactory, navController, isFromSignup)
        }
    }
}

@Composable
fun EditProfileScreen(
    factory: ViewModelProvider.Factory,
    navController: androidx.navigation.NavController,
    isFromSignup: Boolean
) {
    val editProfileViewModel: EditProfileViewModel = viewModel(factory = factory)
    val uiState by editProfileViewModel.uiState.collectAsStateWithLifecycle()

    EditProfileLayout(
        uiState = uiState,
        isFromSignup = isFromSignup,
        onBack = {
            if (navController.previousBackStackEntry != null) {
                navController.popBackStack()
            } else {
                navController.navigate("home") {
                    popUpTo("edit_profile") { inclusive = true }
                }
            }
        },
        onSave = { type, fullName, orgName, regId, web, ind, ph, loc, bio ->
            if (isFromSignup) {
                editProfileViewModel.saveProfile(
                    type, fullName, orgName, regId, web, ind, ph, loc, bio
                )
            } else {
                editProfileViewModel.updateProfile(
                    type, fullName, orgName, regId, web, ind, ph, loc, bio
                )
            }
        }
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate("home") {
                popUpTo(if (isFromSignup) "edit_profile" else "edit_profile/false") { inclusive = true }
            }
        }
    }
}
