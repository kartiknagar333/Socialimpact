package com.example.socialimpact.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.socialimpact.MainActivity
import com.example.socialimpact.SocialImpactApp
import com.example.socialimpact.ui.layouts.EditProfileLayout
import com.example.socialimpact.ui.layouts.HomeLayout
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.AuthViewModel
import com.example.socialimpact.ui.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeActivity : ComponentActivity() {

    @Inject
    lateinit var authViewModelFactory: AuthViewModelFactory

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
            SocialimpactTheme {
                HomeNavigation(
                    factory = authViewModelFactory,
                    startDestination = if (isFromSignup) "edit_profile" else "home",
                    onLogoutTriggered = { navigateToLogin() }
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
    factory: ViewModelProvider.Factory,
    startDestination: String,
    onLogoutTriggered: () -> Unit
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = startDestination) {
        composable("home") {
            HomeLayout(
                onLogout = {
                    coroutineScope.launch {
                        authViewModel.logout()
                        onLogoutTriggered()
                    }
                },
                onEditProfile = {
                    navController.navigate("edit_profile")
                }
            )
        }
        composable("edit_profile") {
            EditProfileLayout(
                onBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("home") {
                            popUpTo("edit_profile") { inclusive = true }
                        }
                    }
                },
                onSave = {
                    // Save logic would go here
                    navController.navigate("home") {
                        popUpTo("edit_profile") { inclusive = true }
                    }
                }
            )
        }
    }
}
