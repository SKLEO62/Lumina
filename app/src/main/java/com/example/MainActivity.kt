package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.screens.LuminaBrowserScreen
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        // STEP 2: Edge-to-edge layout
        enableEdgeToEdge()

        // STEP 3: Render Lumina Browser UI (theme handled dynamically in LuminaBrowserScreen)
        setContent {
            LuminaBrowserScreen()
        }
    }
}
