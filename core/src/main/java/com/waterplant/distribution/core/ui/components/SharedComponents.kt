package com.waterplant.distribution.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.waterplant.distribution.core.ui.theme.Dimens
import com.waterplant.distribution.core.ui.theme.SuccessGreen
import com.waterplant.distribution.core.ui.theme.WarningAmber
import com.waterplant.distribution.core.ui.theme.ErrorRed

/**
 * Primary Large Button (16dp radius)
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.minTouchTarget),
        enabled = enabled,
        shape = MaterialTheme.shapes.small, // 16dp
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Secondary Outline Button (16dp radius)
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.minTouchTarget),
        enabled = enabled,
        shape = MaterialTheme.shapes.small // 16dp
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Summary Card (20dp radius)
 */
@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f), // Make it square
        shape = MaterialTheme.shapes.medium, // 20dp
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Search Bar (16dp radius)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        shape = MaterialTheme.shapes.small, // 16dp
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Status Chip with colors
 */
@Composable
fun StatusChip(status: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Number input for jar counts (16dp radius)
 */
@Composable
fun JarCountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 4) onValueChange(it) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = MaterialTheme.shapes.small, // 16dp
        singleLine = true,
        textStyle = MaterialTheme.typography.displayLarge.copy(textAlign = TextAlign.Center)
    )
}

/**
 * PIN input field (16dp radius)
 */
@Composable
fun PinField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "PIN",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onValueChange(it) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        visualTransformation = PasswordVisualTransformation(),
        shape = MaterialTheme.shapes.small, // 16dp
        singleLine = true
    )
}

/**
 * Report Card
 */
@Composable
fun ReportCard(
    title: String,
    description: String,
    icon: ImageVector,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // 20dp
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(Dimens.grid2)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        icon, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.grid2))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(Dimens.grid2))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onGenerate) {
                    Text("GENERATE")
                }
            }
        }
    }
}

/**
 * Empty state placeholder
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.grid4),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(Dimens.grid2))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(Dimens.grid3))
            action()
        }
    }
}

/**
 * Loading indicator overlay
 */
@Composable
fun LoadingOverlay(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
