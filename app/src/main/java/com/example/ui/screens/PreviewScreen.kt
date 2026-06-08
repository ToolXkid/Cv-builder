package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SimCardDownload
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
import androidx.core.content.FileProvider
import com.example.model.CvData
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen
import java.io.File

@Composable
fun PreviewScreen(viewModel: CvViewModel) {
    val selectedCv by viewModel.selectedCv.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val context = LocalContext.current

    val cv = selectedCv ?: return

    Scaffold(
        topBar = {
            PreviewTopBar(
                onBackClick = { viewModel.navigateTo(Screen.Builder) },
                onShareClick = {
                    val file = viewModel.generatePdfFile(context, cv)
                    if (file != null) {
                        sharePdfFile(context, file)
                    } else {
                        Toast.makeText(context, "Failed to compile CV PDF document.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillStyledWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val file = viewModel.generatePdfFile(context, cv)
                        if (file != null) {
                            Toast.makeText(context, "PDF saved successfully! Path: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error compiling A4 print document.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("export_pdf_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.SimCardDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXPORT PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        containerColor = Color(0xFFF1F5F9) // Classic dark grey designer preview backdrop
    ) { innerPadding ->
        
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ATS Review Blueprint",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Selected layout: ${cv.templateId.replace("_", " ").uppercase()}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // A4 Canvas Paper Mockup representing actual formatting structure
            A4PaperMockup(cv = cv, isPremium = isPremium)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewTopBar(onBackClick: () -> Unit, onShareClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "CV Preview",
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
            IconButton(onClick = onShareClick, modifier = Modifier.testTag("share_pdf_button")) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color(0xFF2196F3)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun A4PaperMockup(cv: CvData, isPremium: Boolean) {
    Card(
        modifier = Modifier
            .width(360.dp)
            .aspectRatio(0.707f) // Exact A4 Aspect Ratio matching paper!
            .padding(horizontal = 16.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .testTag("paper_mockup"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Visual representation matches the chosen template
        when (cv.templateId) {
            "classic_elegance" -> ClassicTemplatePreview(cv = cv, isPremium = isPremium)
            "creative_portfolio" -> CreativeTemplatePreview(cv = cv, isPremium = isPremium)
            else -> ModernTemplatePreview(cv = cv, isPremium = isPremium) // Default "modern_prof"
        }
    }
}

// 1. Modern Professional Visual Preview Representer
@Composable
fun ModernTemplatePreview(cv: CvData, isPremium: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top banner accent
        Text(
            text = cv.name.ifEmpty { "Full Name" },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A365D)
        )
        Text(
            text = cv.title.ifEmpty { "Professional Title" },
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF3182CE)
        )
        
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${cv.email}   |   ${cv.phone}   |   ${cv.location}",
            fontSize = 8.sp,
            color = Color(0xFF718096)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))

        // Summary
        if (cv.summary.isNotEmpty()) {
            Text(text = "PROFESSIONAL SUMMARY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A365D))
            Text(text = cv.summary, fontSize = 8.sp, color = Color(0xFF2D3748), modifier = Modifier.padding(top = 2.dp), maxLines = 4)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Work Experience
        if (cv.experience.isNotEmpty()) {
            Text(text = "WORK EXPERIENCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A365D))
            cv.experience.take(2).forEach { exp ->
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${exp.company} - ${exp.title}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
                        Text(text = "${exp.startDate} - ${exp.endDate}", fontSize = 7.sp, color = Color(0xFF718096))
                    }
                    Text(text = exp.description, fontSize = 7.5.sp, color = Color(0xFF4A5568), maxLines = 3)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Education
        if (cv.education.isNotEmpty()) {
            Text(text = "EDUCATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A365D))
            cv.education.take(1).forEach { edu ->
                Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = edu.institution, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
                        Text(text = edu.degree, fontSize = 7.5.sp, color = Color(0xFF4A5568))
                    }
                    Text(text = edu.year, fontSize = 7.5.sp, color = Color(0xFF718096))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isPremium) {
            WatermarkLabel()
        }
    }
}

// 2. Classic Elegance Visual Preview Representer
@Composable
fun ClassicTemplatePreview(cv: CvData, isPremium: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = cv.name.ifEmpty { "Full Name" },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111111),
            textAlign = TextAlign.Center
        )
        Text(
            text = cv.title.ifEmpty { "Professional Title" },
            fontSize = 11.sp,
            color = Color(0xFF444444),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${cv.email}  •  ${cv.phone}  •  ${cv.location}",
            fontSize = 7.5.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF111111), thickness = 1.dp)

        // Summary
        if (cv.summary.isNotEmpty()) {
            Text(text = "Professional Summary", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
            Text(text = cv.summary, fontSize = 8.sp, color = Color(0xFF333333), modifier = Modifier.padding(top = 2.dp), textAlign = TextAlign.Center, maxLines = 4)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Work Experience
        if (cv.experience.isNotEmpty()) {
            Text(text = "Professional History", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
            cv.experience.take(2).forEach { exp ->
                Column(modifier = Modifier.padding(top = 4.dp).fillStyledWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${exp.company}  —  ${exp.title}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
                        Text(text = "${exp.startDate} - ${exp.endDate}", fontSize = 7f.sp, color = Color(0xFF555555))
                    }
                    Text(text = exp.description, fontSize = 7.5.sp, color = Color(0xFF333333), maxLines = 3)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isPremium) {
            WatermarkLabel()
        }
    }
}

// 3. Creative Portfolio Visual Preview Representer
@Composable
fun CreativeTemplatePreview(cv: CvData, isPremium: Boolean) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Lateral 1/3 panel color representation
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .background(Color(0xFFECEFF1))
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (cv.name.isNotEmpty()) cv.name.take(1).uppercase() else "C",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF37474F)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "CONTACT INFO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = cv.email, fontSize = 6.5.sp, color = Color(0xFF455A64), maxLines = 2)
            Text(text = cv.phone, fontSize = 6.5.sp, color = Color(0xFF455A64))
            Text(text = cv.location, fontSize = 6.5.sp, color = Color(0xFF455A64))

            if (cv.skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "KEY SKILLS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                Spacer(modifier = Modifier.height(4.dp))
                cv.skills.take(5).forEach { skill ->
                    Text(text = "• $skill", fontSize = 7.sp, color = Color(0xFF37474F))
                }
            }
        }

        // Main body right side representer
        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .padding(14.dp)
        ) {
            Text(
                text = cv.name.ifEmpty { "Full Name" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
            Text(
                text = cv.title.ifEmpty { "Professional Title" },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD81B60)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (cv.summary.isNotEmpty()) {
                Text(text = "ABOUT ME", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                Text(text = cv.summary, fontSize = 7.5.sp, color = Color(0xFF37474F), modifier = Modifier.padding(top = 2.dp), maxLines = 4)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (cv.experience.isNotEmpty()) {
                Text(text = "EXPERIENCE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                cv.experience.take(2).forEach { exp ->
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = exp.company, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                            Text(text = exp.startDate, fontSize = 6.5.sp, color = Color(0xFF455A64))
                        }
                        Text(text = exp.title, fontSize = 7.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD81B60))
                        Text(text = exp.description, fontSize = 7.sp, color = Color(0xFF37474F), maxLines = 3)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isPremium) {
                WatermarkLabel()
            }
        }
    }
}

@Composable
fun WatermarkLabel() {
    Text(
        text = "Made with Local CV Cafe",
        fontSize = 7.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF94A3B8),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

fun sharePdfFile(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share CV PDF via"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error launching share sheet.", Toast.LENGTH_SHORT).show()
    }
}

// Extracted padding helpers to clean code
fun Modifier.fillStyledWidth(): Modifier = this.fillMaxWidth()
