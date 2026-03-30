package com.example.socialimpact.ui.layouts

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.model.NeedItem
import com.example.socialimpact.ui.viewmodel.DonationViewModel
import com.google.firebase.app
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet

private const val TAG = "DonationSheetContent"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationSheetContent(
    post: HelpRequestPost,
    viewModel: DonationViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selection by remember { 
        mutableStateOf<Any?>(
            if (post.fundAmount.isNotEmpty()) "fund" else post.dynamicNeeds.firstOrNull()
        ) 
    }
    var amountOrQuantity by remember { mutableStateOf("") }

    // Stripe PaymentSheet initialization
    val paymentSheet = rememberPaymentSheet { result ->
        Log.d(TAG, "PaymentSheet result: $result")
        when (result) {
            is com.stripe.android.paymentsheet.PaymentSheetResult.Completed -> {
                Log.d(TAG, "Payment completed successfully")
                viewModel.handleStripeResult(true)
                onClose()
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Failed -> {
                Log.e(TAG, "Payment failed: ${result.error.message}", result.error)
                viewModel.handleStripeResult(false, result.error.localizedMessage)
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Canceled -> {
                Log.w(TAG, "Payment canceled by user")
                viewModel.handleStripeResult(false, "Payment cancelled.")
            }
        }
    }

    LaunchedEffect(uiState.stripePaymentData) {
        uiState.stripePaymentData?.let { data ->
            Log.d(TAG, "Initializing PaymentSheet with data: $data")
            PaymentConfiguration.init(com.google.firebase.Firebase.app.applicationContext, data.publishableKey)
            
            val customerConfig = PaymentSheet.CustomerConfiguration(
                id = data.customerId,
                ephemeralKeySecret = data.ephemeralKeySecret
            )
            
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = data.paymentIntentClientSecret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "Social Impact Platform",
                    customer = customerConfig,
                    allowsDelayedPaymentMethods = true
                )
            )
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && uiState.stripePaymentData == null) {
            onClose()
            viewModel.clearSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Make a Donation",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color.White,
                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                labelColor = Color.White
            )

            if (post.fundAmount.isNotEmpty()) {
                FilterChip(
                    selected = selection == "fund",
                    onClick = { 
                        selection = "fund"
                        amountOrQuantity = ""
                    },
                    label = { Text("Fund", modifier = Modifier.padding(horizontal = 8.dp)) },
                    leadingIcon = null,
                    trailingIcon = null,
                    shape = CircleShape,
                    modifier = Modifier.height(48.dp),
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selection == "fund",
                        borderColor = Color.White,
                        selectedBorderColor = Color.White
                    )
                )
            }

            post.dynamicNeeds.forEach { item ->
                FilterChip(
                    selected = selection == item,
                    onClick = { 
                        selection = item
                        amountOrQuantity = ""
                    },
                    label = { Text(item.name, modifier = Modifier.padding(horizontal = 8.dp)) },
                    leadingIcon = null,
                    trailingIcon = null,
                    shape = CircleShape,
                    modifier = Modifier.height(48.dp),
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selection == item,
                        borderColor = Color.White,
                        selectedBorderColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val selected = selection) {
            "fund" -> {
                Text(
                    text = "Money goes through Stripe securely to ensure direct impact.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                val inputTextStyle = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = amountOrQuantity,
                    onValueChange = { amountOrQuantity = it },
                    label = {
                        Text("Amount", color = Color.White, fontWeight = FontWeight.SemiBold)
                    },
                    prefix = {
                        Text(
                            "USD ",
                            style = inputTextStyle
                        )
                    },
                    textStyle = inputTextStyle,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.LightGray,
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))


                Button(
                    onClick = { 
                        if (amountOrQuantity.isNotBlank()) {
                            viewModel.startFundDonation(post.id, amountOrQuantity)
                        }
                    },
                    enabled = !uiState.isProcessing && amountOrQuantity.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Payments, 
                            contentDescription = null, 
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Donate Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            is NeedItem -> {
                Text(
                    text = "Physical donations will be officially recorded once received. Your contribution remains in 'Pending' status until confirmation by the recipient.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = selection is NeedItem,
                    enter = slideInHorizontally(animationSpec = tween(durationMillis = 1000)) { -it } + fadeIn()
                ) {
                    val inputTextStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = amountOrQuantity,
                        onValueChange = { amountOrQuantity = it },
                        label = {
                            Text("Quantity", color = Color.White, fontWeight = FontWeight.SemiBold)
                        },
                        prefix = {
                            Text(
                                "${selected.unit} ",
                                style = inputTextStyle
                            )
                        },
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.LightGray,
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { 
                        if (amountOrQuantity.isNotBlank()) {
                            viewModel.submitDonation(post.id, selected.name, amountOrQuantity)
                        }
                    },
                    enabled = !uiState.isProcessing && amountOrQuantity.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Inventory, 
                            contentDescription = null, 
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Donation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onClose, enabled = !uiState.isProcessing) {
            Text("Cancel", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
