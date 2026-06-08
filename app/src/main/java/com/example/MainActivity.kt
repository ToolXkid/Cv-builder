package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.MobileAds
import com.example.ui.screens.BuilderScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PaywallScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TemplateSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.PayfastPaymentScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CvViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this) {}
        
        // Initialize PDFBox resource loader
        try {
            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(this.applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to initialize PDFBoxResourceLoader", e)
        }
        
        // Preload Google AdMob Rewarded Test Ad
        com.example.util.AdMobHelper.loadRewardedAd(this)
        
        // Instantiate using standard robust ViewModelProvider (compatible with all versions)
        val viewModel = ViewModelProvider(this)[CvViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                
                when (val screen = currentScreen) {
                    is Screen.Splash -> SplashScreen(viewModel = viewModel)
                    is Screen.Home -> HomeScreen(viewModel = viewModel)
                    is Screen.Paywall -> PaywallScreen(viewModel = viewModel)
                    is Screen.Builder -> BuilderScreen(viewModel = viewModel)
                    is Screen.TemplateSelect -> TemplateSelectScreen(viewModel = viewModel)
                    is Screen.Preview -> PreviewScreen(viewModel = viewModel)
                    is Screen.Editor -> EditorScreen(viewModel = viewModel)
                    is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                    is Screen.PayfastCheckout -> PayfastPaymentScreen(
                        viewModel = viewModel,
                        planName = screen.planName,
                        amount = screen.amount,
                        frequencyId = screen.frequencyId
                    )
                }
            }
        }
    }
}
