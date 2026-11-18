package com.adveno.tv.core.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Surface & Cosmic Dark Background Palette (Minimal Purple Obsidian)
val TvBackgroundDark = Color(0xFF090514)
val TvSurface = Color(0xFF0D081D)
val TvSurfaceDim = Color(0xFF090514)
val TvSurfaceBright = Color(0xFF261945)
val TvSurfaceDark = Color(0xFF0E091B)
val TvSurfaceElevated = Color(0xFF160E2E)

// Surface Container Hierarchy
val TvSurfaceContainerLowest = Color(0xFF06030D)
val TvSurfaceContainerLow = Color(0xFF0E091B)
val TvSurfaceContainer = Color(0xFF140D28)
val TvSurfaceContainerHigh = Color(0xFF1D1338)
val TvSurfaceContainerHighest = Color(0xFF271A4B)
val TvSurfaceBorder = Color(0x338B5CF6)
val TvOutline = Color(0xFF8B5CF6)
val TvOutlineVariant = Color(0x298B5CF6)

// Primary Brand Colors (Vibrant Neon Purple & Violet Aurora)
val TvPrimary = Color(0xFFA855F7)
val TvPrimaryContainer = Color(0xFF7C3AED)
val TvPrimaryVariant = Color(0xFF8B5CF6)
val TvOnPrimary = Color(0xFFFFFFFF)
val TvOnPrimaryContainer = Color(0xFFFFFFFF)
val TvPrimaryFixed = Color(0xFFDDD6FE)
val TvPrimaryFixedDim = Color(0xFFC4B5FD)

// Gradients
val TvPrimaryGradient = Brush.horizontalGradient(
    listOf(Color(0xFF6366F1), Color(0xFF7C3AED), Color(0xFFA855F7))
)
val TvFocusedPrimaryGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6), Color(0xFFC084FC))
)
val TvGlassBackground = Color(0x2E1E1438)
val TvGlassBorder = Color(0x33FFFFFF)
val TvGlassBorderFocused = Color(0xFFA855F7)

// Status Colors
val TvTertiary = Color(0xFF10B981)
val TvTertiaryContainer = Color(0xFF059669)
val TvOnTertiary = Color(0xFFFFFFFF)
val TvOnTertiaryContainer = Color(0xFFD1FAE5)

// Text & Content Typography Colors
val TvTextPrimary = Color(0xFFFFFFFF)
val TvTextSecondary = Color(0xFFA19EB7)
val TvTextMuted = Color(0xFF7A7593)
val TvTextLavender = Color(0xFFC4B5FD)

// Focus & Selection (Android TV 10-foot High Visibility Glow)
val TvFocusRing = Color(0xFFC084FC)
val TvFocusContainer = Color(0xFF7C3AED)
val TvAccentCyan = Color(0xFF38BDF8)

// Error & Warning
val TvStatusError = Color(0xFFF87171)
val TvErrorContainer = Color(0xFF7F1D1D)
val TvOnError = Color(0xFFFFFFFF)
val TvStatusSuccess = Color(0xFF34D399)
val TvStatusWarning = Color(0xFFFBBF24)
val TvStatusOffline = Color(0xFF7A7593)
