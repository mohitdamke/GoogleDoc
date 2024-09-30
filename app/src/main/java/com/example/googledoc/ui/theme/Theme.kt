package com.example.googledoc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White
import com.itextpdf.kernel.pdf.colorspace.PdfDeviceCs.Gray

private val CustomColorPalette = lightColorScheme(
    primary = Black, // Your fixed primary color
    onPrimary = White, // Fixed text color on primary
    background = White, // Fixed background color
    onBackground = Gray // Fixed text color on background
)

@Composable
fun CustomAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CustomColorPalette,
        content = content
    )
}
//
//@Composable
//fun GoogleDocTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}