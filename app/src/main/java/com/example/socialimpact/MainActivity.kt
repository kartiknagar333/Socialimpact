package com.example.socialimpact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.socialimpact.ui.screens.SplashLayout
import com.example.socialimpact.ui.theme.SocialimpactTheme

/**
 * The main entry point of the Social Impact application.
 *
 * This activity sets up the edge-to-edge display and hosts the root [SplashLayout].
 * As the app grows, this class can be updated to include Jetpack Navigation for 
 * managing screen transitions.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialimpactTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SplashLayout(
                        onGetStarted = {
                            // Handle Get Started action (e.g., navigate to registration)
                        },
                        onLogin = {
                            // Handle Login action (e.g., navigate to login screen)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
