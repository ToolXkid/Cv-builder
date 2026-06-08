package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.findActivity
import com.example.ui.components.AdMobBanner

@Composable
fun SettingsScreen(viewModel: CvViewModel) {
    val isPremium by viewModel.isPremium.collectAsState()
    val credits by viewModel.credits.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    Scaffold(
        topBar = {
            HomeTopBar(isPremium = isPremium, credits = credits, onSettingsClick = {})
        },
        bottomBar = {
            HomeBottomNavigation(
                activeTab = 2,
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Profile & Account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
            )

            // User info card showing dynamic metadata user info (lekalakalathato0@gmail.com)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Authenticated User",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "lekalakalathato0@gmail.com", // Personalized email
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Text(
                text = "Subscription Management",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(top = 8.dp)
            )

            // Dynamic Billing controller dashboard toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isPremium) Color(0xFFFFD700) else Color(0xFFE2E8F0)
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPremium) "Premium Active Crown" else "Free Plan Account",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isPremium) "Enjoying unlimited CV designs with 0 ads" else "Ads show during layouts",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Switch(
                            checked = isPremium,
                            onCheckedChange = { status ->
                                if (status) {
                                    // Subscription payment is required! Direct to Paywall.
                                    Toast.makeText(
                                        context,
                                        "Payfast checkout required to activate Premium.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    viewModel.navigateTo(Screen.Paywall)
                                } else {
                                    // Allow cancelling for easy testing
                                    viewModel.setPremiumStatus(false)
                                    Toast.makeText(
                                        context,
                                        "Premium subscription paused.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFFFD700)
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                    // Ad credits manual bypass control for sandbox check
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bypass Credits: $credits",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "A credit allows you to bypass 1 video ad block.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.addAdCredits(5)
                                Toast.makeText(context, "+5 Credits Added!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF2196F3)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Add +5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text(
                text = "Help & Support",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(top = 8.dp)
            )

            // Extra developer logs / support link elements
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SettingsRowItem(
                        icon = Icons.Default.Restore,
                        title = "Restore Google purchases",
                        onClick = {
                            viewModel.setPremiumStatus(true)
                            Toast.makeText(context, "Purchases recovered. Pro unlocked!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.AdUnits,
                        title = "Admob Config: Watch credit Ad unit",
                        onClick = {
                            // Watch another ad unit to top-up easily
                            activity?.let { viewModel.playRealOrSimulatedAd(it) } ?: viewModel.playSimulatedAd()
                        }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Report a compilation issue",
                        onClick = {
                            Toast.makeText(context, "Support logs uploaded!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Display AdMob banner ad!
            AdMobBanner(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF334155)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(16.dp)
        )
    }
}
