package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                // Official test banner unit ID
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                setAdSize(AdSize.BANNER)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Triggered on dynamic updates if needed
        }
    )
}
