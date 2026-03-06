package com.example.socialimpact

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.socialimpact.di.qualifier.EmailAuth
import com.example.socialimpact.ui.HomeActivity
import com.example.socialimpact.ui.layouts.SigninLayout
import com.example.socialimpact.ui.layouts.SignupLayout
import com.example.socialimpact.ui.layouts.SplashLayout
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.AuthViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authViewModelFactory: AuthViewModelFactory

    @EmailAuth
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as SocialImpactApp)
            .appComponent
            .mainActivityComponent()
            .create()
            .inject(this)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            navigateToHome()
            return
        }

        enableEdgeToEdge()

        setContent {
            SocialimpactTheme {
                AppNavigation(
                    factory = authViewModelFactory,
                    onLoginSuccess = { navigateToHome() }
                )
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

@Composable
fun AppNavigation(
    factory: ViewModelProvider.Factory,
    onLoginSuccess: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            )
        }
    ) {
        composable("splash") {
            SplashLayout(
                onGetStarted = { navController.navigate("signup") },
                onLogin = { navController.navigate("signin") }
            )
        }
        composable("signin") {
            SigninLayout(
                factory = factory,
                onSignup = {
                    navController.navigate("signup") {
                        popUpTo("signin") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onSuccess = onLoginSuccess
            )
        }
        composable("signup") {
            SignupLayout(
                factory = factory,
                onSignin = {
                    navController.navigate("signin") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onSuccess = onLoginSuccess
            )
        }
    }
}
