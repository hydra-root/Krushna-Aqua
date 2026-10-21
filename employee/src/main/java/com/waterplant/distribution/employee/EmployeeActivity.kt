package com.waterplant.distribution.employee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.waterplant.distribution.employee.navigation.EmployeeNavHost
import com.waterplant.distribution.core.ui.theme.WaterJarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmployeeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterJarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EmployeeNavHost()
                }
            }
        }
    }
}
