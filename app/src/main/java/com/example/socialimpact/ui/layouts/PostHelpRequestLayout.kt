package com.example.socialimpact.ui.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialimpact.components.PrimaryNumberField
import com.example.socialimpact.components.PrimaryTextField
import com.example.socialimpact.domain.model.NeedItem
import com.example.socialimpact.ui.state.UploadPostUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostHelpRequestLayout(
    uiState: UploadPostUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onFundAmountChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onToggleNeed: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onAddDynamicNeed: () -> Unit,
    onRemoveDynamicNeed: (Int) -> Unit,
    onUpdateDynamicNeed: (Int, NeedItem) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    var showNeedsDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    // Date Picker logic
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        onStartDateChange(dateFormatter.format(Date(it)))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        onEndDateChange(dateFormatter.format(Date(it)))
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    val isCollapsed = scrollBehavior.state.collapsedFraction > 0.5f
                    Text(
                        text = if (isCollapsed) "Create Post" else "Post Your Help Request and Spread kindness.",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCollapsed) 20.sp else 28.sp,
                        lineHeight = if (isCollapsed) 24.sp else 34.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onDone,
                        enabled = uiState.isFormValid
                    ) {
                        Text(
                            text = "Done",
                            color = if (uiState.isFormValid) MaterialTheme.colorScheme.primary else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PrimaryTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = "Request Title",
                singleLine = false,
                modifier = Modifier.heightIn(min = 56.dp)
            )

            SelectionBox(
                label = "Select Needs",
                selectedItems = uiState.selectedNeeds,
                onAddClick = { showNeedsDialog = true },
                onRemoveItem = onToggleNeed
            )

            SelectionBox(
                label = "Select Category",
                selectedItems = uiState.selectedCategories,
                onAddClick = { showCategoryDialog = true },
                onRemoveItem = onToggleCategory
            )

            if (uiState.selectedNeeds.contains("Fund")) {
                PrimaryNumberField(
                    value = uiState.fundAmount,
                    onValueChange = { onFundAmountChange(it) },
                    label = "How Much Fund You Need",
                    prefix = { Text("Rs. ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "What Your Needs",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    uiState.dynamicNeeds.forEachIndexed { index, item ->
                        NeedRow(
                            item = item,
                            onNameChange = { onUpdateDynamicNeed(index, item.copy(name = it)) },
                            onUnitChange = { onUpdateDynamicNeed(index, item.copy(unit = it)) },
                            onQuantityChange = { onUpdateDynamicNeed(index, item.copy(quantity = it)) },
                            onRemove = { onRemoveDynamicNeed(index) }
                        )
                        if (index < uiState.dynamicNeeds.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        }
                    }

                    TextButton(
                        onClick = onAddDynamicNeed,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(4.dp))
                        Text("ADD NEED", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            PrimaryTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = "Type Description",
                singleLine = false,
                modifier = Modifier.heightIn(min = 120.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Start Grace", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,color = MaterialTheme.colorScheme.onBackground)
                DateBox(value = uiState.startDate, placeholder = "Select Date") { 
                    showStartDatePicker = true 
                }
                
                Text("End of Grace", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,color = MaterialTheme.colorScheme.onBackground)
                DateBox(value = uiState.endDate, placeholder = "Select Date") { 
                    showEndDatePicker = true 
                }
            }

            PrimaryTextField(
                value = uiState.address,
                onValueChange = onAddressChange,
                label = "Full Address",
                leadingIcon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showNeedsDialog) {
        MultiSelectionDialog(
            title = "What Your Needs",
            options = listOf("Fund", "Volunteers", "Food", "Clothes", "Shelters", "Medical", "Others"),
            selectedItems = uiState.selectedNeeds,
            onToggle = onToggleNeed,
            onDismiss = { showNeedsDialog = false }
        )
    }

    if (showCategoryDialog) {
        MultiSelectionDialog(
            title = "Select Category",
            options = listOf("Education", "Emergencies", "Medical", "Animal", "Children", "Environment", "Others"),
            selectedItems = uiState.selectedCategories,
            onToggle = onToggleCategory,
            onDismiss = { showCategoryDialog = false }
        )
    }
}

@Composable
private fun SelectionBox(label: String, selectedItems: List<String>, onAddClick: () -> Unit, onRemoveItem: (String) -> Unit) {
    OutlinedCard(
        onClick = onAddClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.tertiary)
            }
            if (selectedItems.isNotEmpty()) {
                FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedItems.forEach { item ->
                        InputChip(
                            selected = true,
                            enabled = true,
                            onClick = { onRemoveItem(item) },
                            label = { Text(item, color = MaterialTheme.colorScheme.onBackground) },
                            trailingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.Cancel, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                ) 
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeedRow(item: NeedItem, onNameChange: (String) -> Unit, onUnitChange: (String) -> Unit, onQuantityChange: (String) -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            TextField(
                value = item.name,
                onValueChange = onNameChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                placeholder = { Text("Item") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            var expanded by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { expanded = true }.padding(horizontal = 8.dp)) {
                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.tertiary)
                Text(item.unit, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.tertiary)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Pcs", "Kg", "Ltr", "Gm").forEach { u ->
                    DropdownMenuItem(text = { Text(u) }, onClick = { onUnitChange(u); expanded = false })
                }
            }
            TextField(
                value = item.quantity,
                placeholder = {
                    Text(
                        text = "0",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == '.' }
                    onQuantityChange(filtered)
                },
                singleLine = true,
                modifier = Modifier.width(70.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground
                ),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        IconButton(onClick = onRemove) { Icon(Icons.Default.Cancel, null, tint = Color.Gray) }
    }
}

@Composable
private fun DateBox(value: String, placeholder: String, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick,  shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
        ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, null)
            Spacer(Modifier.width(12.dp))
            Text(text = value.ifEmpty { placeholder }, color = if (value.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun MultiSelectionDialog(
    title: String, 
    options: List<String>, 
    selectedItems: List<String>, 
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground) },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = selectedItems.contains(option)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggle(option) },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("DONE", color = MaterialTheme.colorScheme.primary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = MaterialTheme.colorScheme.primary) } },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.onPrimary
    )
}
