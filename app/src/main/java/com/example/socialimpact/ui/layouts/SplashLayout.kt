package com.example.socialimpact.ui.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialimpact.R
import com.example.socialimpact.components.AppItem
import com.example.socialimpact.components.LightButton
import com.example.socialimpact.components.PrimaryButton

/**
 * A maintainable and scalable Splash Layout for the Social Impact application.
 * 
 * This component uses state hoisting for its actions, making it highly testable 
 * and easy to integrate with navigation libraries.
 *
 * @param onGetStarted Action to perform when the primary "Get Started" button is clicked.
 * @param onLogin Action to perform when the secondary "Login" button is clicked.
 * @param modifier Modifier to be applied to the root layout.
 */
@Composable
fun SplashLayout(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header Section: Centralized Branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        }

        // Content Section: Core Value Propositions
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.socialimpact),
                contentDescription = "Social Impact Logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground), // Added tint color here
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Social Impact",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)

            )
            AppItem(
                title = "Community Support",
                description = "Connect with people nearby to help or get support for local initiatives.",
                icon = Icons.Rounded.Diversity3
            )
            AppItem(
                title = "Volunteering Events",
                description = "Find and join volunteering opportunities that match your skills and interests.",
                icon = Icons.Rounded.VolunteerActivism
            )
            AppItem(
                title = "Impact Tracking",
                description = "Monitor your contributions and see the positive change you're making.",
                icon = Icons.Rounded.AutoGraph
            )
        }

        // Footer Section: Primary and Secondary Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(
                text = "Get Started !",
                onClick = onGetStarted
            )
            LightButton(
                text = "Have an account ? Login Now",
                onClick = onLogin
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashLayoutPreview() {
    SplashLayout(
        onGetStarted = {},
        onLogin = {}
    )
}
