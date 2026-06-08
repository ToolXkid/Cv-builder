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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.util.findActivity
import com.example.ui.components.AdMobBanner
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun PaywallScreen(viewModel: CvViewModel) {
    val isPremium by viewModel.isPremium.collectAsState()
    val isAdPlaying by viewModel.isAdPlaying.collectAsState()
    val adCountdown by viewModel.adCountdown.collectAsState()
    val credits by viewModel.credits.collectAsState()

    // Render ad-playing screen overlay directly if active
    if (isAdPlaying) {
        SimulatedAdPlayer(countdown = adCountdown)
    } else {
        PaywallContent(
            viewModel = viewModel,
            credits = credits,
            isPremium = isPremium
        )
    }
}

@Composable
fun PaywallContent(
    viewModel: CvViewModel,
    credits: Int,
    isPremium: Boolean
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

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
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
                
                Text(
                    text = "Subscription Cafe",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
                
                Spacer(modifier = Modifier.width(48.dp)) // Equalizer spacing
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Crown graphics / branding headers
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Elevate to Premium Plan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Unleash maximum employment prospects with elite CV themes.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pricing Suggestion Panel (South African Rand Payfast Subscriptions)
            PremiumPurchaseCard(
                onSubscribeSelected = { planName, amount, frequencyId ->
                    viewModel.navigateTo(Screen.PayfastCheckout(planName, amount, frequencyId))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Benefit breakdown details
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaywallBenefitRow(text = "❌  No visual advertisements or video gates ever")
                PaywallBenefitRow(text = "♾️  Unlimited CV exports and PDF template creations")
                PaywallBenefitRow(text = "✨  Access to elite high-contrast layouts (Creative/Classic)")
                PaywallBenefitRow(text = "🧼  Completely remove \"Made with Local CV Cafe\" watermark")
                PaywallBenefitRow(text = "⚡  Smarter, unlimited AI-assisted resume scan parses")
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Or Watched Ad choice divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text(
                    text = " OR WATCH REWARDED AD ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Free option row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activity?.let { viewModel.playRealOrSimulatedAd(it) } ?: viewModel.playSimulatedAd() }
                    .testTag("watch_ad_option"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Watch Ad for 1 Free Credit",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "Unlocks builder sandbox & edits immediately.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Restore Purchases Link
            Text(
                text = "Restore Purchases",
                fontSize = 12.sp,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        val activeSub = viewModel.subscriptionPlan.value
                        if (activeSub.isNotEmpty()) {
                            viewModel.setPremiumStatus(true, activeSub)
                            Toast.makeText(context, "Successfully restored your Payfast subscription: $activeSub", Toast.LENGTH_LONG).show()
                            viewModel.navigateTo(Screen.Home)
                        } else {
                            Toast.makeText(context, "No active Payfast subscription profile found on this device.", Toast.LENGTH_LONG).show()
                        }
                    }
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Display AdMob banner ad!
            AdMobBanner(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
fun PremiumPurchaseCard(
    onSubscribeSelected: (planName: String, amount: Double, frequencyId: Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Monthly, 1: Yearly
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subscribe_button_container"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "SECURE PAYFAST SUBSCRIPTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Plan switcher tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) Color(0xFF1E293B) else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Monthly Plan",
                        color = if (selectedTab == 0) Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) Color(0xFF1E293B) else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Yearly Plan",
                            color = if (selectedTab == 1) Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Surface(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SAVE 50%",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pricing visuals
            if (selectedTab == 0) {
                Text(
                    text = "R79.00 / month",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Billed monthly via PayFast. Cancel anytime.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            } else {
                Text(
                    text = "R450.00 / year",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Equivalent to R37.50/month. Billed annually.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (selectedTab == 0) {
                        onSubscribeSelected("Monthly Subscription", 79.0, 3)
                    } else {
                        onSubscribeSelected("Yearly Subscription", 450.0, 6)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("subscribe_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Subscribe via Payfast Gateway",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PaywallBenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569)
        )
    }
}

@Composable
fun SimulatedAdPlayer(countdown: Int) {
    // Elegant system-themed dynamic video ad player
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header information indicating sponsored content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Sponsored Ad",
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Reward in ${countdown}s",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Aesthetic Dynamic Graphic to represent the standard Ad Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Moving coffee dynamic loader representation
                Icon(
                    imageVector = Icons.Default.LocalCafe,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(96.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Unlocking Resume Builder...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "Local CV Cafe - Simple, Fast, Beautiful Offline-first layouts.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            // Bottom informational banner
            Text(
                text = "Reward unlocks immediately after timer completes. Do not close.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
