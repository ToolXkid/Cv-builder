package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobHelper {
    private const val TAG = "AdMobHelper"
    
    // Official test rewarded unit ID
    private const val REWARDED_TEST_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var mRewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun loadRewardedAd(context: Context, onLoaded: (Boolean) -> Unit = {}) {
        if (mRewardedAd != null || isAdLoading) {
            onLoaded(mRewardedAd != null)
            return
        }
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_TEST_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "Ad failed to load: ${adError.message}")
                mRewardedAd = null
                isAdLoading = false
                onLoaded(false)
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                mRewardedAd = rewardedAd
                isAdLoading = false
                onLoaded(true)
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        val rewardedAd = mRewardedAd
        if (rewardedAd != null) {
            rewardedAd.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
            // Clear reference so we reload next time
            mRewardedAd = null
            loadRewardedAd(activity)
            onAdDismissed()
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet. Loading...")
            loadRewardedAd(activity) { loaded ->
                if (loaded) {
                    showRewardedAd(activity, onRewardEarned, onAdDismissed)
                } else {
                    // Fallback to offline local ad simulation if internet or loading fails
                    onAdDismissed()
                }
            }
        }
    }
    
    fun isAdLoaded(): Boolean = mRewardedAd != null
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
