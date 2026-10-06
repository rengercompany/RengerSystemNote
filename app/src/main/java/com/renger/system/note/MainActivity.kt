package com.renger.system.note

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.renger.system.note.ui.navigation.NavGraph
import com.renger.system.note.ui.theme.RengerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RengerTheme {
                NavGraph()
            }
        }
    }
}