package com.example.evidencijaclanova

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class EvidencijaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Primijeni spremljenu dark mode preferencu pri pokretanju app
        val prefs = getSharedPreferences("postavke", MODE_PRIVATE)
        val darkMode = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
