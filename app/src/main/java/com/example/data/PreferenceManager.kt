package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "local_cv_cafe_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_CREATION_CREDITS = "creation_credits"
        private const val KEY_SUBSCRIPTION_PLAN = "subscription_plan"
    }

    var isPremium: Boolean
        get() = prefs.getBoolean(KEY_IS_PREMIUM, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PREMIUM, value).apply()

    var subscriptionPlan: String
        get() = prefs.getString(KEY_SUBSCRIPTION_PLAN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SUBSCRIPTION_PLAN, value).apply()

    var creationCredits: Int
        get() = prefs.getInt(KEY_CREATION_CREDITS, 0)
        set(value) = prefs.edit().putInt(KEY_CREATION_CREDITS, value).apply()

    fun useCredit(): Boolean {
        if (isPremium) return true
        val current = creationCredits
        return if (current > 0) {
            creationCredits = current - 1
            true
        } else {
            false
        }
    }

    fun addCredit(count: Int = 1) {
        creationCredits = creationCredits + count
    }
}
