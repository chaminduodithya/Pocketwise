package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

@Immutable
data class SereneExtendedColors(
    val income: Color,
    val incomeContainer: Color,
    val onIncomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val onExpenseContainer: Color,
    val primaryFixed: Color,
    val onPrimaryFixed: Color,
    val tertiaryFixed: Color,
    val onTertiaryFixed: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val isDark: Boolean
)

val LocalSereneColors = staticCompositionLocalOf {
    SereneExtendedColors(
        income = SecondaryGreen,
        incomeContainer = SecondaryContainerMint,
        onIncomeContainer = OnSecondaryContainerGreen,
        expense = ErrorRed,
        expenseContainer = ErrorContainerLight,
        onExpenseContainer = OnErrorContainerRed,
        primaryFixed = PrimaryFixed,
        onPrimaryFixed = OnPrimaryFixed,
        tertiaryFixed = TertiaryFixed,
        onTertiaryFixed = OnTertiaryFixed,
        surfaceContainerLowest = SurfaceContainerLowestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerTeal,
    onPrimaryContainer = OnPrimaryContainerMint,
    secondary = SecondaryGreen,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = SecondaryContainerMint,
    onSecondaryContainer = OnSecondaryContainerGreen,
    tertiary = TertiaryCrimson,
    onTertiary = OnPrimaryWhite,
    tertiaryContainer = TertiaryContainerRed,
    onTertiaryContainer = Color(0xFFFFCBC5),
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerHighestLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorRed,
    onError = OnPrimaryWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerRed,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = PrimaryFixedDim
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerHighestDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = Color(0xFF690005),
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = SurfaceDark,
    inversePrimary = PrimaryTeal
)

val SereneShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun FinanceTrackerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = if (useDarkTheme) {
        SereneExtendedColors(
            income = SecondaryDark,
            incomeContainer = SecondaryContainerDark,
            onIncomeContainer = OnSecondaryContainerDark,
            expense = ErrorDark,
            expenseContainer = ErrorContainerDark,
            onExpenseContainer = OnErrorContainerDark,
            primaryFixed = PrimaryContainerDark,
            onPrimaryFixed = OnPrimaryContainerDark,
            tertiaryFixed = TertiaryContainerDark,
            onTertiaryFixed = OnTertiaryContainerDark,
            surfaceContainerLowest = SurfaceContainerLowestDark,
            surfaceContainerLow = SurfaceContainerLowDark,
            surfaceContainer = SurfaceContainerDark,
            surfaceContainerHigh = SurfaceContainerHighDark,
            surfaceContainerHighest = SurfaceContainerHighestDark,
            isDark = true
        )
    } else {
        SereneExtendedColors(
            income = SecondaryGreen,
            incomeContainer = SecondaryContainerMint,
            onIncomeContainer = OnSecondaryContainerGreen,
            expense = ErrorRed,
            expenseContainer = ErrorContainerLight,
            onExpenseContainer = OnErrorContainerRed,
            primaryFixed = PrimaryFixed,
            onPrimaryFixed = OnPrimaryFixed,
            tertiaryFixed = TertiaryFixed,
            onTertiaryFixed = OnTertiaryFixed,
            surfaceContainerLowest = SurfaceContainerLowestLight,
            surfaceContainerLow = SurfaceContainerLowLight,
            surfaceContainer = SurfaceContainerLight,
            surfaceContainerHigh = SurfaceContainerHighLight,
            surfaceContainerHighest = SurfaceContainerHighestLight,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalSereneColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SereneTypography,
            shapes = SereneShapes,
            content = content
        )
    }
}

object SereneTheme {
    val colors: SereneExtendedColors
        @Composable get() = LocalSereneColors.current
}
