package com.example.parcial2breweryguide.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografia de la pantalla de Compose.
 *
 * Solo redefinimos los tres estilos que usa el detalle; los demas quedan con los
 * valores que trae Material 3 de fabrica.
 */
val Typography = Typography(
    // Nombre de la cerveceria.
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    // Texto normal: direccion, telefono, sitio web.
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    // Etiquetas chicas arriba de cada dato.
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)
