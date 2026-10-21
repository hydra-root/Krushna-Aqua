package com.waterplant.distribution.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 Shapes following specific instructions
val AppShapes = Shapes(
    // Buttons, TextFields
    small = RoundedCornerShape(16.dp),
    
    // Cards, FAB
    medium = RoundedCornerShape(20.dp),
    
    // Dialogs
    large = RoundedCornerShape(24.dp)
)
