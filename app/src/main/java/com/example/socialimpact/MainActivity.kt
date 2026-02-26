package com.example.socialimpact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.socialimpact.components.LightButton
import com.example.socialimpact.components.PrimaryButton
import com.example.socialimpact.ui.theme.SocialimpactTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialimpactTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BottomButtonsLayout(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BottomButtonsLayout(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        PrimaryButton(
            text = "Get Started !",
            onClick = { /* TODO */ }
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        LightButton(
            text = "Have an account ? Login Now",
            onClick = { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GreetingPreview() {
    SocialimpactTheme {
        BottomButtonsLayout()
    }
}
