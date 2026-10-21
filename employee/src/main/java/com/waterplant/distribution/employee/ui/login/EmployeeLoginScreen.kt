package com.waterplant.distribution.employee.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.ui.components.PinField
import com.waterplant.distribution.core.ui.theme.PrimaryBlue

@Composable
fun EmployeeLoginScreen(
    onLoginSuccess: (String) -> Unit,
    viewModel: EmployeeLoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var employeeId by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val customIp by viewModel.customIp.collectAsState()
    var showIpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val employee = (loginState as LoginState.Success).employee
            onLoginSuccess(employee.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryBlue, Color(0xFF004D40))
                )
            )
    ) {
        // Settings/IP icon
        IconButton(
            onClick = { showIpDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Wifi,
                contentDescription = "Connection Settings",
                tint = Color.White.copy(alpha = 0.5f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Water Jar Delivery",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Employee Login",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it; viewModel.resetState() },
                        label = { Text("Your Name") },
                        placeholder = { Text("e.g. Ravi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = loginState !is LoginState.Loading
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PinField(
                        value = pin,
                        onValueChange = { pin = it; viewModel.resetState() },
                        label = "PIN"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (loginState) {
                        is LoginState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Authenticating...", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        is LoginState.Error -> {
                            Text(
                                text = (loginState as LoginState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (employeeId.isNotBlank() && pin.length >= 4) {
                                viewModel.login(context, employeeId.trim(), pin)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = employeeId.isNotBlank() && pin.length >= 4 && loginState !is LoginState.Loading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Help text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Wifi, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (customIp.isBlank()) "First login requires Plant Phone hotspot. After that, login works offline."
                    else "Connecting to: $customIp",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showIpDialog) {
        var tempIp by remember { mutableStateOf(customIp) }
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("Plant Phone IP") },
            text = {
                Column {
                    Text("If auto-detection fails, enter the IP shown on the Admin dashboard.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempIp,
                        onValueChange = { tempIp = it },
                        placeholder = { Text("e.g. 192.168.43.1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setCustomIp(tempIp)
                    showIpDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setCustomIp("")
                    showIpDialog = false
                }) {
                    Text("Reset Auto")
                }
            }
        )
    }
}
