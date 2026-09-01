package com.example.scaffold.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// A touch rounder than Material3's baseline scale (small unchanged; medium/large/extraLarge
// bumped up a step) for a softer, more contemporary feel on cards, sheets, and the FAB.
val Shapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(32.dp),
    )
