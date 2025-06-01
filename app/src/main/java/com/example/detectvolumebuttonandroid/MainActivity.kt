package com.example.detectvolumebuttonandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.detectvolumebuttonandroid.ui.theme.DetectVolumeButtonAndroidTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DetectVolumeButtonAndroidTheme {
                AppContent()
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun AppContent() {
        var isServiceRunning by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Volume Button Service") })
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isServiceRunning) "Service is Running" else "Service is Stopped",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        toggleService(isServiceRunning)
                        isServiceRunning = !isServiceRunning
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isServiceRunning) "Stop Service" else "Start Service")
                }
            }
        }
    }

    private fun toggleService(isRunning: Boolean) {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        if (isRunning) {
            Log.d("Recording", "Stopping service")
            stopService(serviceIntent)
        } else {
            Log.d("Recording", "Starting service")
            serviceIntent.action = ForegroundService.ACTION_FOREGROUND_WAKELOCK
            startService(serviceIntent)
        }
    }
}
