package com.mdp.caremate.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

/**
 * Utility untuk mengecek dan mengatur status Premium per-user.
 * Status disimpan di SharedPreferences dengan key unik berdasarkan UID user.
 */
object PremiumUtils {

    private const val PREFS_NAME = "CareMatePrefs"

    /**
     * Mendapatkan UID user yang sedang login.
     */
    private fun getCurrentUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    /**
     * Mengecek apakah user yang sedang login adalah Premium.
     * Key di SharedPreferences: "isPremium_<uid>"
     */
    fun isPremium(context: Context): Boolean {
        val uid = getCurrentUid() ?: return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("isPremium_$uid", false)
    }

    /**
     * Mengatur status Premium untuk user yang sedang login.
     */
    fun setPremium(context: Context, premium: Boolean) {
        val uid = getCurrentUid() ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("isPremium_$uid", premium).apply()
    }
}
