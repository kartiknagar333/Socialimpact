package com.example.socialimpact.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.components.PrimaryTextField
import com.example.socialimpact.di.component.SocialImpactApp
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.UploadViewModel
import com.example.socialimpact.ui.viewmodel.UploadViewModelFactory
import javax.inject.Inject

class UploadActivity : ComponentActivity() {

    @Inject
    lateinit var uploadViewModelFactory: UploadViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as SocialImpactApp)
            .appComponent
            .uploadActivityComponent()
            .create()
            .inject(this)

        enableEdgeToEdge()
        setContent {
            SocialimpactTheme {
                PostHelpRequestScreen(
                    factory = uploadViewModelFactory,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostHelpRequestScreen(
    factory: UploadViewModelFactory,
    onBack: () -> Unit,
    viewModel: UploadViewModel = viewModel(factory = factory)
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    var requestTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var fundAmount by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("India") }

    val selectedNeeds = remember { mutableStateListOf<String>() }
    val selectedCategories = remember { mutableStateListOf<String>() }
    val dynamicNeeds = remember { mutableStateListOf<NeedItem>(NeedItem()) }
    
    var showNeedsDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Post Your Help Request and Spread kindness.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

            // 1. Request Title
            PrimaryTextField(
                value = requestTitle,
                onValueChange = { requestTitle = it },
                label = "Request Title",
                singleLine = false,
                modifier = Modifier.heightIn(min = 56.dp)
            )

            // 2. Select Needs
            SelectionBox(
                label = "Select Needs",
                selectedItems = selectedNeeds,
                onAddClick = { showNeedsDialog = true },
                onRemoveItem = { selectedNeeds.remove(it) }
            )

            // 3. Select Category
            SelectionBox(
                label = "Select Category",
                selectedItems = selectedCategories,
                onAddClick = { showCategoryDialog = true },
                onRemoveItem = { selectedCategories.remove(it) }
            )

            // Fund Input
            if (selectedNeeds.contains("Fund")) {
                PrimaryTextField(
                    value = fundAmount,
                    onValueChange = { fundAmount = it },
                    label = "How Much Fund You Need",
                    prefix = { Text("$. ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                )
            }

            // What Your Needs Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "What Your Needs",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    dynamicNeeds.forEachIndexed { index, item ->
                        NeedRow(
                            item = item,
                            onNameChange = { dynamicNeeds[index] = item.copy(name = it) },
                            onUnitChange = { dynamicNeeds[index] = item.copy(unit = it) },
                            onQuantityChange = { dynamicNeeds[index] = item.copy(quantity = it) },
                            onRemove = { if (dynamicNeeds.size > 1) dynamicNeeds.removeAt(index) }
                        )
                        if (index < dynamicNeeds.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        }
                    }

                    TextButton(
                        onClick = { dynamicNeeds.add(NeedItem()) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(4.dp))
                        Text("ADD NEED", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            // 4. Add Photo Section
           // DashedPhotoBox()

            // 5. Description
            PrimaryTextField(
                value = description,
                onValueChange = { description = it },
                label = "Type Description",
                singleLine = false,
                modifier = Modifier.heightIn(min = 120.dp)
            )

            // 6. Start Grace (Dates)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Start Grace", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                DateBox(value = startDate, placeholder = "Select Date") { /* Open Picker */ }
                
                Text("End of Grace", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                DateBox(value = endDate, placeholder = "Select Date") { /* Open Picker */ }
            }

            // 7. Country Dropdown
            CountryBox(selectedCountry) { selectedCountry = it }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialogs
    if (showNeedsDialog) {
        MultiSelectionDialog(
            title = "What Your Needs",
            options = listOf("Fund", "Volunteers", "Food", "Clothes", "Shelters", "Medical", "Others"),
            selectedItems = selectedNeeds,
            onDismiss = { showNeedsDialog = false }
        )
    }

    if (showCategoryDialog) {
        MultiSelectionDialog(
            title = "Select Category",
            options = listOf("Education", "Emergencies", "Medical", "Animal", "Children", "Environment", "Others"),
            selectedItems = selectedCategories,
            onDismiss = { showCategoryDialog = false }
        )
    }
}

@Composable
fun SelectionBox(label: String, selectedItems: List<String>, onAddClick: () -> Unit, onRemoveItem: (String) -> Unit) {
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
fun DashedPhotoBox() {
    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F6FF))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(color = Color(0xFFD1D9FF), style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(40.dp), Color(0xFF1A237E))
            Text("ADD PHOTO", color = Color(0xFF1A237E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

data class NeedItem(val name: String = "", val unit: String = "Pcs", val quantity: String = "0.0")

@Composable
fun NeedRow(item: NeedItem, onNameChange: (String) -> Unit, onUnitChange: (String) -> Unit, onQuantityChange: (String) -> Unit, onRemove: () -> Unit) {
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
                placeholder = { Text("Item Name") },
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
                onValueChange = onQuantityChange,
                modifier = Modifier.width(70.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            )
        }
        IconButton(onClick = onRemove) { Icon(Icons.Default.Cancel, null, tint = Color.Gray) }
    }
}

@Composable
fun DateBox(value: String, placeholder: String, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, null)
            Spacer(Modifier.width(12.dp))
            Text(text = value.ifEmpty { placeholder }, color = if (value.isEmpty()) Color.Gray else Color.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryBox(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        PrimaryTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = "",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("India", "USA", "UK").forEach { c ->
                DropdownMenuItem(text = { Text(c) }, onClick = { onSelect(c); expanded = false })
            }
        }
    }
}

@Composable
fun MultiSelectionDialog(title: String, options: List<String>, selectedItems: MutableList<String>, onDismiss: () -> Unit) {
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
                        onClick = { if (isSelected) selectedItems.remove(option) else selectedItems.add(option) },
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
