package com.example.socialimpact.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            if (icon != null) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text = text,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmButtonText,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = dismissButtonText,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
fun LogoutConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AppConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm,
        title = "Logout",
        text = "Are you sure you want to logout from your account?",
        confirmButtonText = "Logout",
        icon = Icons.AutoMirrored.Filled.Logout
    )
}
