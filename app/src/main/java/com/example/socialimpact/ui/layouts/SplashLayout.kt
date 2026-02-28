package com.example.socialimpact.ui.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.socialimpact.components.GlassyAuthBackground
import com.example.socialimpact.components.PrimaryButton

@Composable
fun SplashLayout(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassyAuthBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Social Impact",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
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

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = "Get Started !",
                    onClick = onGetStarted
                )
                
                TextButton(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account? Sign In",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
