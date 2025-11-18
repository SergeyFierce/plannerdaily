package com.sergeyfierce.dailyplanner.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private fun baseLightScheme() = lightColorScheme(
    primary = SeedPrimary,
    secondary = SeedSecondary,
    tertiary = SeedTertiary
)

private fun baseDarkScheme() = darkColorScheme(
    primary = SeedPrimary,
    secondary = SeedSecondary,
    tertiary = SeedTertiary
)

@Composable
fun DailyPlannerTheme(
    darkTheme: Boolean,
    useDynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val supportsDynamic = useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        supportsDynamic && darkTheme -> dynamicDarkColorScheme(context)
        supportsDynamic && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> baseDarkScheme()
        else -> baseLightScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
