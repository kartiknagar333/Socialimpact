package com.example.socialimpact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.socialimpact.ui.layouts.HomeLayout
import com.example.socialimpact.ui.layouts.SigninLayout
import com.example.socialimpact.ui.layouts.SignupLayout
import com.example.socialimpact.ui.layouts.SplashLayout
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Firebase Auth automatically remembers the logged-in user securely.
        val currentUser = FirebaseAuth.getInstance().currentUser
        val startDestination = if (currentUser != null) "home" else "splash"

        setContent {
            SocialimpactTheme {
                AppNavigation(startDestination = startDestination)
            }
        }
    }
}

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    // Removed innerPadding from NavHost to allow backgrounds to cover system bars (Edge-to-Edge)
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                onBack = { navController.popBackStack() },
                onSuccess = { 
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("signup") {
            SignupLayout(
                onBack = { navController.popBackStack() },
                onSuccess = { 
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeLayout(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("splash") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
