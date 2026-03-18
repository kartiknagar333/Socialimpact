package com.example.socialimpact.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A glassy background with spreaded blurred circles for Auth screens.
 * The animation is now perfectly seamless and runs at a higher speed.
 */
@Composable
fun GlassyAuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.tertiary
    val tertiaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    val infiniteTransition = rememberInfiniteTransition(label = "glassy_bg_transition")
    
    // Increased speed (from 10s to 7s) for a more energetic feel.
    // Animating a normalized progress from 0 to 1 to ensure perfect integer cycles for all circles.
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_progress"
    )

    // Helper to calculate 2 * PI * progress
    val angle = progress * 2f * Math.PI.toFloat()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Blurred spreaded circles with perfectly seamless endless movement
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(100.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            // Circle 1: Top Right - 1 full cycle per 7s
            drawCircle(
                color = primaryColor.copy(alpha = 0.4f),
                radius = size.width * 0.5f,
                center = Offset(
                    x = size.width * (0.8f + 0.12f * sin(angle)),
                    y = size.height * (0.2f + 0.12f * cos(angle))
                )
            )
            // Circle 2: Bottom Left - 1 full cycle per 7s (different direction)
            drawCircle(
                color = tertiaryColor.copy(alpha = 0.3f),
                radius = size.width * 0.6f,
                center = Offset(
                    x = size.width * (0.2f + 0.15f * cos(angle + 1f)),
                    y = size.height * (0.8f + 0.12f * sin(angle + 1f))
                )
            )
            // Circle 3: Center - 2 full cycles per 7s (High speed variation)
            // Multiplier 2 ensures it finishes exactly where it started
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = size.width * 0.4f,
                center = Offset(
                    x = size.width * (0.5f + 0.08f * sin(angle * 2f)),
                    y = size.height * (0.5f + 0.08f * cos(angle * 2f))
                )
            )
        }

        // Glass tint layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Color.White.copy(alpha = 0.1f)
                    } else {
                        Color.White.copy(alpha = 0.4f)
                    }
                )
        )

        // Main content
        content()
    }
}
