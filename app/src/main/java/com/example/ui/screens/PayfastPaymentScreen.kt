package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PayfastPaymentScreen(
    viewModel: CvViewModel,
    planName: String,
    amount: Double,
    frequencyId: Int
) {
    val context = LocalContext.current
    var isWebLoading by remember { mutableStateOf(true) }
    var webErrorOccurred by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Callback helper upon successful billing completion
    val handleSubscriptionSuccess = {
        viewModel.setPremiumStatus(true, "$planName (R${amount})")
        showSuccessDialog = true
    }

    val handleSubscriptionCancel = {
        Toast.makeText(context, "Payfast Checkout cancelled.", Toast.LENGTH_LONG).show()
        viewModel.navigateTo(Screen.Paywall)
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss during success */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(32.dp)
                    )
                    Text("Payment Authorized!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Congratulations!",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Your $planName of R${amount} was set up successfully via Payfast secure token recurring billing.",
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "All Premium resume designs are now unlocked, and video/sponsor advertisements are completely disabled.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    onClick = {
                        showSuccessDialog = false
                        viewModel.navigateTo(Screen.Home)
                    }
                ) {
                    Text("Enter Premium Cafe")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { handleSubscriptionCancel() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Cancel Checkout",
                            tint = Color(0xFF475569)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Secure PayFast Checkout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "SSL Secure Signature",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "sandbox.payfast.co.za (Rands ZAR)",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ZAR R${amount}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sandbox Tester Console",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        }
                        
                        Text(
                            text = "For Offline/Demo verification",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f).testTag("simulate_payment_success"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = { handleSubscriptionSuccess() }
                        ) {
                            Text("Simulate Success", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f).testTag("simulate_payment_cancel"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))),
                            shape = RoundedCornerShape(8.dp),
                            onClick = { handleSubscriptionCancel() }
                        ) {
                            Text("Simulate Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main WebView rendering the PayFast payment post
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isWebLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isWebLoading = false
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: ""
                                if (url.contains("payfast/success") || url.contains("success")) {
                                    handleSubscriptionSuccess()
                                    return true
                                }
                                if (url.contains("payfast/cancel") || url.contains("cancel")) {
                                    handleSubscriptionCancel()
                                    return true
                                }
                                return false
                            }
                        }

                        // Formulate POST parameters for sandbox checkout subscription
                        val returnUrl = "https://example.com/payfast/success"
                        val cancelUrl = "https://example.com/payfast/cancel"
                        val notifyUrl = "https://example.com/payfast/notify"
                        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                        val postParameters = StringBuilder()
                            .append("merchant_id=").append(URLEncoder.encode("10000100", "UTF-8"))
                            .append("&merchant_key=").append(URLEncoder.encode("46f0df5e1c07c", "UTF-8"))
                            .append("&return_url=").append(URLEncoder.encode(returnUrl, "UTF-8"))
                            .append("&cancel_url=").append(URLEncoder.encode(cancelUrl, "UTF-8"))
                            .append("&notify_url=").append(URLEncoder.encode(notifyUrl, "UTF-8"))
                            .append("&name_first=").append(URLEncoder.encode("CV", "UTF-8"))
                            .append("&name_last=").append(URLEncoder.encode("CafeUser", "UTF-8"))
                            .append("&email_address=").append(URLEncoder.encode("lekalakalathato0@gmail.com", "UTF-8"))
                            .append("&m_payment_id=").append(URLEncoder.encode("sub-user-" + UUID.randomUUID().toString().take(6), "UTF-8"))
                            .append("&amount=").append(URLEncoder.encode(amount.toString(), "UTF-8"))
                            .append("&item_name=").append(URLEncoder.encode("CV Cafe $planName", "UTF-8"))
                            .append("&item_description=").append(URLEncoder.encode("PayFast Secure Recurring Billing Subscription Setup", "UTF-8"))
                            .append("&subscription_type=").append(URLEncoder.encode("1", "UTF-8"))
                            .append("&billing_date=").append(URLEncoder.encode(formattedDate, "UTF-8"))
                            .append("&recurring_amount=").append(URLEncoder.encode(amount.toString(), "UTF-8"))
                            .append("&frequency=").append(URLEncoder.encode(frequencyId.toString(), "UTF-8"))
                            .append("&cycles=").append(URLEncoder.encode("0", "UTF-8"))
                            .toString()

                        try {
                            postUrl(
                                "https://sandbox.payfast.co.za/eng/process",
                                postParameters.toByteArray(StandardCharsets.UTF_8)
                            )
                        } catch (e: Exception) {
                            webErrorOccurred = true
                            isWebLoading = false
                        }
                    }
                }
            )

            // Web Loading Overlaid indicator
            AnimatedVisibility(
                visible = isWebLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = Color(0xFF1D4ED8))
                        Text(
                            text = "Loading Secure Payfast Server...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Web Load error (e.g., if internet is fully disabled in testing sandbox container)
            if (webErrorOccurred) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Cloud Offline",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payfast Gate Unreachable",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sandbox billing could not connect to Payfast servers directly. Please utilize the Sandbox Tester Console below to simulate successful recurring authorization immediately.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
