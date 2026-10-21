package com.waterplant.distribution.admin.ui.login

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waterplant.distribution.core.ui.components.PinField
import com.waterplant.distribution.core.ui.theme.PrimaryBlue
import com.waterplant.distribution.core.ui.theme.PrimaryBlueDark

@Composable
fun AdminLoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val adminPin = sharedPrefs.getString("admin_pin", "1234") ?: "1234"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryBlue, PrimaryBlueDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Water Plant",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Admin Control Panel",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Login card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Enter Admin PIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    PinField(
                        value = pin,
                        onValueChange = { pin = it; error = "" },
                        label = "PIN Code"
                    )

                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (pin == adminPin) {
                                onLoginSuccess()
                            } else {
                                error = "Invalid PIN"
                                pin = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = pin.length >= 4,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "PIN can be changed from Settings",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
