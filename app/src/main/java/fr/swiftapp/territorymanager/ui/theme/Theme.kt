package fr.swiftapp.territorymanager.ui.theme

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    surfaceVariant = Color(0xFF342B2C),
    outline = Color(0xFFA68B7E),
    primary = Color(0xFFB3C5FF),
    onPrimary = Color(0xFF152C68),
    background = Color(0xFF251913),
    secondaryContainer = Color(0xFF3F4B26),
    onSecondaryContainer = Color(0xFFCBDBA9),
    surface = Color(0xFF251913),
    onSurface = Color(0xFFF5DED4),
    onSurfaceVariant = Color(0xFFF5DED4),
    errorContainer = Color(0xFFFF1F1F),
    onErrorContainer = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    surfaceVariant = Color(0xFFFCDCCD),
    outline = Color(0xFF8A7164),
    primary = Color(0xFF475C99),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBFA),
    secondaryContainer = Color(0xFFD9E9B6),
    onSecondaryContainer = Color(0xFF1B2608),
    surface = Color(0xFFFFFBFA),
    onSurface = Color(0xFF251913),
    onSurfaceVariant = Color(0xFF584237),
    errorContainer = Color(0xFFC01313),
    onErrorContainer = Color(0xFFFFFFFF)
)

@Composable
fun TerritoryManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    Log.d("TEST", "theme")

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            Log.d("TEST", "dynamicColor")
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}