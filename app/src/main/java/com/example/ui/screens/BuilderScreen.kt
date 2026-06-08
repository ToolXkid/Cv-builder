package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CvData
import com.example.model.Education
import com.example.model.WorkExperience
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun BuilderScreen(viewModel: CvViewModel) {
    val selectedCv by viewModel.selectedCv.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    val cv = selectedCv ?: return

    Scaffold(
        topBar = {
            BuilderTopBar(
                cvName = cv.name,
                onBackClick = { viewModel.navigateTo(Screen.Home) },
                onPreviewClick = {
                    // Navigate directly to preview screen
                    viewModel.navigateTo(Screen.Preview)
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { viewModel.saveCurrentCv() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_cv_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "SAVE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal scrollable Tabs
            BuilderTabsHeader(
                activeTab = activeTab,
                onTabSelect = { viewModel.changeActiveTab(it) }
            )

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    0 -> PersonalInfoTab(cv = cv, onUpdate = { viewModel.updateSelectedCv(it) })
                    1 -> WorkExperienceTab(cv = cv, onUpdate = { viewModel.updateSelectedCv(it) })
                    2 -> EducationTab(cv = cv, onUpdate = { viewModel.updateSelectedCv(it) })
                    3 -> SkillsTab(cv = cv, onUpdate = { viewModel.updateSelectedCv(it) })
                    4 -> SummaryTab(cv = cv, onUpdate = { viewModel.updateSelectedCv(it) })
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderTopBar(
    cvName: String,
    onBackClick: () -> Unit,
    onPreviewClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Editing CV: ${cvName.ifEmpty { "New Draft" }}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1E293B)
                )
            }
        },
        actions = {
            Button(
                onClick = onPreviewClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEFF6FF),
                    contentColor = Color(0xFF2196F3)
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("preview_trigger_button")
            ) {
                Text(
                    text = "Preview",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun BuilderTabsHeader(activeTab: Int, onTabSelect: (Int) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = activeTab,
        containerColor = Color.White,
        contentColor = Color(0xFF2196F3),
        edgePadding = 16.dp,
        divider = { HorizontalDivider(color = Color(0xFFF1F5F9)) }
    ) {
        val tabNames = listOf("Personal Info", "Experience", "Education", "Skills", "Summary")
        tabNames.forEachIndexed { index, name ->
            Tab(
                selected = activeTab == index,
                onClick = { onTabSelect(index) },
                text = {
                    Text(
                        text = name,
                        fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            )
        }
    }
}

// ------------------------------
// TAB 1: PERSONAL INFORMATION
// ------------------------------
@Composable
fun PersonalInfoTab(cv: CvData, onUpdate: (CvData) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Personal Information",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            )

            // Simulated circular camera avatar icon matching Image 3
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .clickable { /* Select profile image simulation */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Tap to choose photo (Option)",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )

            UiTextField(
                label = "Full Name",
                value = cv.name,
                onValueChange = { onUpdate(cv.copy(name = it)) }
            )

            UiTextField(
                label = "Professional Title",
                value = cv.title,
                onValueChange = { onUpdate(cv.copy(title = it)) },
                placeholder = "e.g., Software Engineer"
            )

            UiTextField(
                label = "Email Address",
                value = cv.email,
                onValueChange = { onUpdate(cv.copy(email = it)) }
            )

            UiTextField(
                label = "Phone Number",
                value = cv.phone,
                onValueChange = { onUpdate(cv.copy(phone = it)) }
            )

            UiTextField(
                label = "LinkedIn Profile",
                value = cv.linkedin,
                onValueChange = { onUpdate(cv.copy(linkedin = it)) }
            )

            UiTextField(
                label = "Location",
                value = cv.location,
                onValueChange = { onUpdate(cv.copy(location = it)) },
                placeholder = "e.g., San Francisco, CA"
            )
        }
    }
}

// ------------------------------
// TAB 2: WORK EXPERIENCE
// ------------------------------
@Composable
fun WorkExperienceTab(cv: CvData, onUpdate: (CvData) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Work Experience",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Button(
                onClick = {
                    val newExpList = cv.experience + WorkExperience()
                    onUpdate(cv.copy(experience = newExpList))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF2196F3)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (cv.experience.isEmpty()) {
            EmptyBuilderSectionCard(text = "No work experience history specified. Add some to build completeness!")
        } else {
            cv.experience.forEachIndexed { index, exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Experience #${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )

                            IconButton(
                                onClick = {
                                    val newExpList = cv.experience.toMutableList().apply { removeAt(index) }
                                    onUpdate(cv.copy(experience = newExpList))
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }

                        UiTextField(
                            label = "Company Name",
                            value = exp.company,
                            onValueChange = { newVal ->
                                val updated = cv.experience.toMutableList()
                                updated[index] = exp.copy(company = newVal)
                                onUpdate(cv.copy(experience = updated))
                            }
                        )

                        UiTextField(
                            label = "Job Title",
                            value = exp.title,
                            onValueChange = { newVal ->
                                val updated = cv.experience.toMutableList()
                                updated[index] = exp.copy(title = newVal)
                                onUpdate(cv.copy(experience = updated))
                            }
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                UiTextField(
                                    label = "Start Date",
                                    value = exp.startDate,
                                    onValueChange = { newVal ->
                                        val updated = cv.experience.toMutableList()
                                        updated[index] = exp.copy(startDate = newVal)
                                        onUpdate(cv.copy(experience = updated))
                                    },
                                    placeholder = "e.g., Jun 2020"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                UiTextField(
                                    label = "End Date",
                                    value = exp.endDate,
                                    onValueChange = { newVal ->
                                        val updated = cv.experience.toMutableList()
                                        updated[index] = exp.copy(endDate = newVal)
                                        onUpdate(cv.copy(experience = updated))
                                    },
                                    placeholder = "Present"
                                )
                            }
                        }

                        UiTextField(
                            label = "Description / Responsibilities",
                            value = exp.description,
                            onValueChange = { newVal ->
                                val updated = cv.experience.toMutableList()
                                updated[index] = exp.copy(description = newVal)
                                onUpdate(cv.copy(experience = updated))
                            },
                            singleLine = false,
                            maxLines = 4
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------
// TAB 3: EDUCATION HISTORY
// ------------------------------
@Composable
fun EducationTab(cv: CvData, onUpdate: (CvData) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Education History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Button(
                onClick = {
                    val newEduList = cv.education + Education()
                    onUpdate(cv.copy(education = newEduList))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF2196F3)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (cv.education.isEmpty()) {
            EmptyBuilderSectionCard(text = "No academic history specified. Add one to show on your CV.")
        } else {
            cv.education.forEachIndexed { index, edu ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Education #${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )

                            IconButton(
                                onClick = {
                                    val newEduList = cv.education.toMutableList().apply { removeAt(index) }
                                    onUpdate(cv.copy(education = newEduList))
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }

                        UiTextField(
                            label = "Institution / University",
                            value = edu.institution,
                            onValueChange = { newVal ->
                                val updated = cv.education.toMutableList()
                                updated[index] = edu.copy(institution = newVal)
                                onUpdate(cv.copy(education = updated))
                            }
                        )

                        UiTextField(
                            label = "Degree earned",
                            value = edu.degree,
                            onValueChange = { newVal ->
                                val updated = cv.education.toMutableList()
                                updated[index] = edu.copy(degree = newVal)
                                onUpdate(cv.copy(education = updated))
                            },
                            placeholder = "e.g. Master of Business Administration"
                        )

                        UiTextField(
                            label = "Graduation Year",
                            value = edu.year,
                            onValueChange = { newVal ->
                                val updated = cv.education.toMutableList()
                                updated[index] = edu.copy(year = newVal)
                                onUpdate(cv.copy(education = updated))
                            },
                            placeholder = "e.g. 2024"
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------
// TAB 4: KEY SKILLS BLOCK
// ------------------------------
@Composable
fun SkillsTab(cv: CvData, onUpdate: (CvData) -> Unit) {
    var newSkillText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Expertise & Technical Skills",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            // Dynamic additions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = newSkillText,
                    onValueChange = { newSkillText = it },
                    label = { Text("Add New Skill", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        if (newSkillText.isNotBlank()) {
                            val newList = cv.skills + newSkillText.trim()
                            onUpdate(cv.copy(skills = newList))
                            newSkillText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("ADD", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chip grid representational block
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cv.skills.isEmpty()) {
                    Text(
                        text = "No skills added yet. Insert chips above to improve completeness.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Quick wrapping grid flow row simulated representation
                    val items = cv.skills
                    for (i in items.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkillChipCard(
                                text = items[i],
                                onDelete = {
                                    val newList = cv.skills.toMutableList().apply { removeAt(i) }
                                    onUpdate(cv.copy(skills = newList))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            if (i + 1 < items.size) {
                                SkillChipCard(
                                    text = items[i + 1],
                                    onDelete = {
                                        val newList = cv.skills.toMutableList().apply { removeAt(i + 1) }
                                        onUpdate(cv.copy(skills = newList))
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkillChipCard(text: String, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "Delete",
                tint = Color(0xFF64748B),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

// ------------------------------
// TAB 5: PROFESSIONAL SUMMARY
// ------------------------------
@Composable
fun SummaryTab(cv: CvData, onUpdate: (CvData) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Professional Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            UiTextField(
                label = "About Yourself",
                value = cv.summary,
                onValueChange = { onUpdate(cv.copy(summary = it)) },
                placeholder = "Write a paragraph highlighting your goals, accomplishments, and skills...",
                singleLine = false,
                maxLines = 10
            )
        }
    }
}

// ------------------------------
// WIDGET ELEMENTS
// ------------------------------
@Composable
fun UiTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, fontSize = 13.sp) },
            singleLine = singleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EmptyBuilderSectionCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}
