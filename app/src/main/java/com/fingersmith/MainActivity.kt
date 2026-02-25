package com.fingersmith

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.fingersmith.ui.AppScreen
import com.fingersmith.ui.MainViewModel
import com.fingersmith.ui.theme.FingerSmithTheme

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FingerSmithTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppScreen(vm)
                }
            }
        }
    }
}
