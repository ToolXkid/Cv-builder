package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CvData
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: CvViewModel) {
    val cvs by viewModel.cvList.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val credits by viewModel.credits.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            HomeTopBar(isPremium = isPremium, credits = credits, onSettingsClick = {
                viewModel.navigateTo(Screen.Settings)
            })
        },
        bottomBar = {
            HomeBottomNavigation(
                activeTab = 0,
                onTabClick = { tabIndex ->
                    when (tabIndex) {
                        0 -> viewModel.navigateTo(Screen.Home)
                        1 -> viewModel.navigateTo(Screen.TemplateSelect)
                        2 -> viewModel.navigateTo(Screen.Settings)
                    }
                }
            )
        },
        floatingActionButton = {
            CreateCvFloatingActionButton(onCreateClick = {
                viewModel.checkAdAndProceed {
                    viewModel.selectCvForEditing(null)
                    viewModel.navigateTo(Screen.Builder)
                }
            })
        },
        containerColor = Color(0xFFF7F9FC) // Soft grey-white background matching images
    ) { innerPadding ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My CVs",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                // Extra fast Edit/Paste PDF action
                Card(
                    modifier = Modifier
                        .testTag("import_pdf_button")
                        .clickable {
                            viewModel.checkAdAndProceed {
                                viewModel.navigateTo(Screen.Editor)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Article,
                            contentDescription = "PDF Scan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Import PDF/TXT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cvs.isEmpty()) {
                EmptyCvsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(cvs) { cv ->
                        CvListItemCard(
                            cv = cv,
                            onEditClick = {
                                viewModel.checkAdAndProceed {
                                    viewModel.selectCvForEditing(cv)
                                    viewModel.navigateTo(Screen.Builder)
                                }
                            },
                            onDeleteClick = {
                                viewModel.deleteCv(cv)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(isPremium: Boolean, credits: Int, onSettingsClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalCafe,
                    contentDescription = null,
                    tint = Color(0xFF2196F3)
                )
                Text(
                    text = "Local CV Cafe",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
        },
        actions = {
            // Plan management badge
            if (isPremium) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFD700))),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium Status",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "PREMIUM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8A6D1C)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE))),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Credits",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Credits: $credits",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
            }
            
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF64748B)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
        modifier = Modifier.testTag("app_bar")
    )
}

@Composable
fun HomeBottomNavigation(activeTab: Int, onTabClick: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = activeTab == 0,
            onClick = { onTabClick(0) },
            icon = { Icon(imageVector = if (activeTab == 0) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = activeTab == 1,
            onClick = { onTabClick(1) },
            icon = { Icon(imageVector = if (activeTab == 1) Icons.Filled.MenuBook else Icons.Outlined.MenuBook, contentDescription = "Templates") },
            label = { Text("Templates", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = activeTab == 2,
            onClick = { onTabClick(2) },
            icon = { Icon(imageVector = if (activeTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 11.sp) }
        )
    }
}

@Composable
fun CreateCvFloatingActionButton(onCreateClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onCreateClick,
        containerColor = Color(0xFF2196F3),
        contentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp)) },
        text = { Text("Create New CV", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(bottom = 16.dp, end = 8.dp)
            .testTag("create_cv_fab")
    )
}

@Composable
fun CvListItemCard(
    cv: CvData,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val formattedDate = remember(cv.lastModified) {
        val sdf = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
        sdf.format(Date(cv.lastModified))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
            .testTag("cv_card_${cv.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion Dial matches screens
            CompletionProgressDial(
                percentage = cv.completionPercentage,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cv.name.ifEmpty { "Unspecified Name" },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = cv.title.ifEmpty { "Curriculum Vitae" },
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Modified: $formattedDate",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Option Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit CV",
                        tint = Color(0xFF1E293B)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete CV",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
fun CompletionProgressDial(percentage: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 5.dp.toPx()
            
            // Draw background track
            drawCircle(
                color = Color(0xFFE2E8F0),
                style = Stroke(width = strokeWidth)
            )
            
            // Draw active progress
            drawArc(
                color = Color(0xFF2196F3),
                startAngle = -90f,
                sweepAngle = (percentage / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
fun EmptyCvsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to the Cafe!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tap 'Create New CV' to design your elegant CV, or paste details into our smart AI Parser.",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
