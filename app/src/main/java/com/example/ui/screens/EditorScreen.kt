package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun EditorScreen(viewModel: CvViewModel) {
    val isScanning by viewModel.isScanning.collectAsState()
    val scanError by viewModel.scanError.collectAsState()
    var pastedResumeText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            val extractedText = com.example.util.PdfTextExtractor.extractText(context, selectedUri)
            if (!extractedText.isNullOrBlank()) {
                pastedResumeText = extractedText
                Toast.makeText(context, "Successfully imported CV content!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to parse document text. Make sure it is a valid PDF or TXT file.", Toast.LENGTH_LONG).show()
            }
        }
    }

    var showImportDialog by remember { mutableStateOf(false) }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Resume Content", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            text = { Text("Choose whether to pick an actual PDF or TXT file from your device storage, or preload an expert sandbox CV template for quick testing.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    onClick = {
                        showImportDialog = false
                        try {
                            filePickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Storage picker not supported. Preloading Sandbox template instead.", Toast.LENGTH_LONG).show()
                            pastedResumeText = getPreloadedMockResumeText()
                        }
                    }
                ) {
                    Text("Select PDF/TXT from Device")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportDialog = false
                        pastedResumeText = getPreloadedMockResumeText()
                        Toast.makeText(context, "Loaded sandbox CV template!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Load Sandbox Template", color = Color(0xFF475569))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E293B)
                    )
                }

                Text(
                    text = "AI CV Scanner",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // High-value onboarding block explaining the AI parser
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Gemini Parser",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Paste text of an old CV, copy-paste LinkedIn summaries, or upload a text file. Gemini AI will categorize contact details, dynamic achievements, and skills automatically!",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Quick trigger simulated file picker card matches images
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showImportDialog = true
                    }
                    .testTag("file_picker_trigger"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Import Existing CV File",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Supports .pdf & .txt (Pick from device storage or load sandbox template)",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Text Input Box for Manual Paste
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Paste Resume Content",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )

                    OutlinedTextField(
                        value = pastedResumeText,
                        onValueChange = { pastedResumeText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("pasted_cv_input"),
                        placeholder = {
                            Text(
                                text = "Paste raw CV credentials, email templates, or dynamic summaries here...",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Display LLM Parsing Errors if they occur
            scanError?.let { error ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Text(text = error, fontSize = 11.sp, color = Color(0xFF991B1B))
                    }
                }
            }

            // Call to action parsing button with dynamic loading screen overlay
            if (isScanning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Gemini is parsing credentials...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Analyzing experience timelines, academics and skillsets...",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (pastedResumeText.isNotBlank()) {
                            viewModel.parseResumeWithAi(pastedResumeText)
                        } else {
                            Toast.makeText(context, "Please paste resume text or tap 'Import' first.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("ai_scan_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI SCAN & CONVERT", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun getPreloadedMockResumeText(): String {
    return """
        Marcus Vance
        Mobile Architect
        marcusvance@email.com
        +1 (555) 432-8877
        Seattle, WA
        linkedin.com/in/marcusvance
        
        Summary:
        Skilled software architect specializing in Android applications, reactive paradigms, offline persistence, and Material Design guidelines. Committed to producing readable, testable code for millions of users.
        
        Recent Work History:
        Alpha Stream Apps - Lead Android Engineer (Feb 2021 - Present)
        Design core product features, refactored existing SQLite integrations to modern Room schemas, and mentored 3 graduates. Reduced UI lag by 20% using Jetpack Compose drawing optimizations.
        
        Technical Education:
        University of Washington
        Bachelor of Engineering in Computer Systems, 2020
        
        Known Skillsets:
        Kotlin, Android UI, Compose Canvas, Room DB, Dagger Hilt, Retrofit Networking, API Integrations, Unit Tests.
    """.trimIndent()
}
