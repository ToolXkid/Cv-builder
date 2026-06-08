package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun TemplateSelectScreen(viewModel: CvViewModel) {
    val isPremium by viewModel.isPremium.collectAsState()
    val selectedCv by viewModel.selectedCv.collectAsState()
    
    // Track template being audited for zoom-in drawer modal matches image 2
    var auditingTemplate by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            HomeTopBar(isPremium = isPremium, credits = 0, onSettingsClick = {
                viewModel.navigateTo(Screen.Settings)
            })
        },
        bottomBar = {
            HomeBottomNavigation(
                activeTab = 1,
                onTabClick = { tabIndex ->
                    when (tabIndex) {
                        0 -> viewModel.navigateTo(Screen.Home)
                        1 -> viewModel.navigateTo(Screen.TemplateSelect)
                        2 -> viewModel.navigateTo(Screen.Settings)
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select a Template",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose a premium theme optimized for ATS search indexes.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grid templates layouts
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TemplateGridItem(
                        title = "Modern Professional",
                        desc = "Sleek blue accent bar.",
                        isLocked = false,
                        isActive = selectedCv?.templateId == "modern_prof",
                        thumbnailPainter = { drawModernThumbnail() },
                        modifier = Modifier.weight(1f),
                        onClick = { auditingTemplate = "modern_prof" }
                    )

                    TemplateGridItem(
                        title = "Classic Elegance",
                        desc = "Centered Serif headings.",
                        isLocked = !isPremium, // Premium-locked template
                        isActive = selectedCv?.templateId == "classic_elegance",
                        thumbnailPainter = { drawClassicThumbnail() },
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isPremium) {
                                viewModel.navigateTo(Screen.Paywall)
                            } else {
                                auditingTemplate = "classic_elegance"
                            }
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TemplateGridItem(
                        title = "Creative Portfolio",
                        desc = "Asymmetric slate sidebar.",
                        isLocked = !isPremium, // Premium-locked template
                        isActive = selectedCv?.templateId == "creative_portfolio",
                        thumbnailPainter = { drawCreativeThumbnail() },
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isPremium) {
                                viewModel.navigateTo(Screen.Paywall)
                            } else {
                                auditingTemplate = "creative_portfolio"
                            }
                        }
                    )

                    // Card block representing extra premium template expansions
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.85f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Expanding Café...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Zoom Drawer Modal selected to mimic Image 2 Select / Cancel
    auditingTemplate?.let { templateId ->
        TemplateAuditDialog(
            templateId = templateId,
            onCancel = { auditingTemplate = null },
            onSelect = {
                // Apply chosen layout template to CV if editing one
                selectedCv?.let {
                    viewModel.updateSelectedCv(it.copy(templateId = templateId))
                    viewModel.saveCurrentCv()
                }
                auditingTemplate = null
            }
        )
    }
}

@Composable
fun TemplateGridItem(
    title: String,
    desc: String,
    isLocked: Boolean,
    isActive: Boolean,
    thumbnailPainter: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .clickable { onClick() }
            .testTag("template_item_${title.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isActive) Color(0xFF2196F3) else Color(0xFFE2E8F0)
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF1F5F9))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Renders the tiny layout schematic thumbnail
                thumbnailPainter()

                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked theme",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155),
                    maxLines = 1
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TemplateAuditDialog(
    templateId: String,
    onCancel: () -> Unit,
    onSelect: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("template_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (templateId) {
                        "classic_elegance" -> "Classic Elegance"
                        "creative_portfolio" -> "Creative Portfolio"
                        else -> "Modern Professional"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated full-page layout preview matching Image 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    when (templateId) {
                        "classic_elegance" -> drawClassicFullPreview()
                        "creative_portfolio" -> drawCreativeFullPreview()
                        else -> drawModernFullPreview()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Image 2 Buttons matches: SELECT / CANCEL at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF64748B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onSelect,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_template_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SELECT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ------------------------------------
// THUMBNAIL CANVAS DRAWINGS REPS
// ------------------------------------
@Composable
fun drawModernThumbnail() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw Navy top header
        drawRect(Color(0xFF1A365D), size = size.copy(height = size.height * 0.22f))
        
        // Draw tiny schematic rows on grey page
        drawRect(Color(0xFFE2E8F0), topLeft = androidx.compose.ui.geometry.Offset(20f, size.height * 0.35f), size = size.copy(width = size.width - 40f, height = 4f))
        drawRect(Color(0xFFCBD5E1), topLeft = androidx.compose.ui.geometry.Offset(20f, size.height * 0.45f), size = size.copy(width = size.width - 80f, height = 3f))
        drawRect(Color(0xFFE2E8F0), topLeft = androidx.compose.ui.geometry.Offset(20f, size.height * 0.55f), size = size.copy(width = size.width - 40f, height = 4f))
    }
}

@Composable
fun drawClassicThumbnail() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Classic style centered lines
        val lineW = size.width * 0.6f
        val lineX = (size.width - lineW) / 2
        drawRect(Color(0xFF334155), topLeft = androidx.compose.ui.geometry.Offset(lineX, 20f), size = size.copy(width = lineW, height = 6f))
        
        drawRect(Color(0xFFE2E8F0), topLeft = androidx.compose.ui.geometry.Offset(20f, size.height * 0.3f), size = size.copy(width = size.width - 40f, height = 4f))
        drawRect(Color(0xFFCBD5E1), topLeft = androidx.compose.ui.geometry.Offset(20f, size.height * 0.4f), size = size.copy(width = size.width - 40f, height = 3f))
    }
}

@Composable
fun drawCreativeThumbnail() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw 1/3 sidebar slate-color panel
        drawRect(Color(0xFF455A64), size = size.copy(width = size.width * 0.35f))
        
        // Draw main body items on side
        val lMargin = size.width * 0.42f
        drawRect(Color(0xFFE2E8F0), topLeft = androidx.compose.ui.geometry.Offset(lMargin, size.height * 0.15f), size = size.copy(width = size.width * 0.5f, height = 5f))
        drawRect(Color(0xFFCBD5E1), topLeft = androidx.compose.ui.geometry.Offset(lMargin, size.height * 0.28f), size = size.copy(width = size.width * 0.5f, height = 3f))
    }
}

// ------------------------------------
// FULL DIALOG CANVAS DETAIL REPS
// ------------------------------------
@Composable
fun drawModernFullPreview() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color(0xFF1A365D), size = size.copy(height = size.height * 0.25f))
        
        val margin = 30f
        var yPos = size.height * 0.35f
        
        // Profile info details simulated lines
        drawRect(Color(0xFF3182CE), topLeft = androidx.compose.ui.geometry.Offset(margin, yPos), size = size.copy(width = size.width * 0.5f, height = 12f))
        yPos += 30f
        
        for (i in 0..4) {
            drawRect(Color(0xFFE2E8F0), topLeft = androidx.compose.ui.geometry.Offset(margin, yPos), size = size.copy(width = size.width - (margin*2), height = 6f))
            yPos += 16f
            drawRect(Color(0xFFF1F5F9), topLeft = androidx.compose.ui.geometry.Offset(margin, yPos), size = size.copy(width = size.width - 60f, height = 4f))
            yPos += 12f
        }
    }
}

@Composable
fun drawClassicFullPreview() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerLineW = size.width * 0.7f
        val startX = (size.width - centerLineW) / 2
        
        // Center-aligned name simulated line
        drawRect(Color(0xFF1E293B), topLeft = androidx.compose.ui.geometry.Offset(startX, 30f), size = size.copy(width = centerLineW, height = 14f))
        drawRect(Color(0xFF64748B), topLeft = androidx.compose.ui.geometry.Offset(startX + 40f, 60f), size = size.copy(width = centerLineW - 80f, height = 8f))
        
        // Strong section spacer line
        drawRect(Color(0xFF1E293B), topLeft = androidx.compose.ui.geometry.Offset(30f, 90f), size = size.copy(width = size.width - 60f, height = 3f))
        
        var yPos = 110f
        for (i in 0..3) {
            // Section Titles
            drawRect(Color(0xFF334155), topLeft = androidx.compose.ui.geometry.Offset(30f, yPos), size = size.copy(width = size.width * 0.4f, height = 10f))
            yPos += 18f
            drawRect(Color(0xFFCBD5E1), topLeft = androidx.compose.ui.geometry.Offset(30f, yPos), size = size.copy(width = size.width - 60f, height = 5f))
            yPos += 14f
            drawRect(Color(0xFFF1F5F9), topLeft = androidx.compose.ui.geometry.Offset(30f, yPos), size = size.copy(width = size.width - 120f, height = 4f))
            yPos += 22f
        }
    }
}

@Composable
fun drawCreativeFullPreview() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Slate accent column representation
        drawRect(Color(0xFF37474F), size = size.copy(width = size.width * 0.35f))
        
        // Side bar features lines representation
        var sideY = 40f
        for (i in 0..2) {
            drawRect(Color(0xFFB0BEC5), topLeft = androidx.compose.ui.geometry.Offset(15f, sideY), size = size.copy(width = size.width * 0.22f, height = 8f))
            sideY += 16f
            drawRect(Color(0xFF546E7A), topLeft = androidx.compose.ui.geometry.Offset(15f, sideY), size = size.copy(width = size.width * 0.22f, height = 5f))
            sideY += 28f
        }

        // Main body features representation
        val mainX = size.width * 0.38f
        var mainY = 30f
        
        drawRect(Color(0xFFD81B60), topLeft = androidx.compose.ui.geometry.Offset(mainX, mainY), size = size.copy(width = size.width * 0.5f, height = 15f))
        mainY += 30f
        
        for (i in 0..3) {
            drawRect(Color(0xFF37474F), topLeft = androidx.compose.ui.geometry.Offset(mainX, mainY), size = size.copy(width = size.width * 0.45f, height = 10f))
            mainY += 18f
            drawRect(Color(0xFFECEFF1), topLeft = androidx.compose.ui.geometry.Offset(mainX, mainY), size = size.copy(width = size.width * 0.55f, height = 6f))
            mainY += 14f
            drawRect(Color(0xFFCFD8DC), topLeft = androidx.compose.ui.geometry.Offset(mainX, mainY), size = size.copy(width = size.width * 0.45f, height = 4f))
            mainY += 20f
        }
    }
}
