package com.example.socialimpact.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.socialimpact.components.PrimaryTextField

enum class ProfileType { PERSON, NGO, CORPORATION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileLayout(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var profileType by remember { mutableStateOf(ProfileType.PERSON) }
    
    // Common fields
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    // Type specific fields
    var fullName by remember { mutableStateOf("") } // For Person
    var organizationName by remember { mutableStateOf("") } // For NGO/Corp
    var registrationId by remember { mutableStateOf("") } // For NGO/Corp
    var website by remember { mutableStateOf("") } // For NGO/Corp
    var industry by remember { mutableStateOf("") } // For Corporation

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSave,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp,
                    focusedElevation = 10.dp,
                    hoveredElevation = 10.dp
                )
            ) {
                Text(text = "Save Profile")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(80.dp).padding(20.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                SmallFloatingActionButton(
                    onClick = { /* TODO: Change photo */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.offset(x = (-4).dp, y = (-4).dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", modifier = Modifier.size(16.dp))
                }
            }

            // Profile Type Selector
            Text(
                text = "Select Profile Type",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )
            
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                ProfileType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ProfileType.entries.size),
                        onClick = { profileType = type },
                        selected = profileType == type,
                        icon = {}, // Removes the checkmark icon when selected
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.tertiary,
                            activeContentColor = Color.Black,

                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            // Dynamic Fields based on Profile Type
            when (profileType) {
                ProfileType.PERSON -> {
                    PrimaryTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )
                }
                ProfileType.NGO, ProfileType.CORPORATION -> {
                    PrimaryTextField(
                        value = organizationName,
                        onValueChange = { organizationName = it },
                        label = if (profileType == ProfileType.NGO) "NGO Name" else "Company Name",
                        leadingIcon = if (profileType == ProfileType.NGO) Icons.Default.Groups else Icons.Default.Business
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryTextField(
                        value = registrationId,
                        onValueChange = { registrationId = it },
                        label = "Registration / Tax ID",
                        leadingIcon = Icons.Default.AppRegistration
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = "Website URL",
                        leadingIcon = Icons.Default.Language
                    )
                }
            }

            if (profileType == ProfileType.CORPORATION) {
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = "Industry",
                    leadingIcon = Icons.Default.Category
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                leadingIcon = Icons.Default.Phone
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location",
                leadingIcon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryTextField(
                value = bio,
                onValueChange = { bio = it },
                label = if (profileType == ProfileType.PERSON) "Bio" else "About / Mission",
                leadingIcon = Icons.Default.Description,
                singleLine = false,
                modifier = Modifier.heightIn(min = 120.dp)
            )

            Spacer(modifier = Modifier.height(100.dp)) // Extra space to scroll past the FAB
        }
    }
}
