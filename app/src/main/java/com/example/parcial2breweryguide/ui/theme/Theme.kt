package com.example.parcial2breweryguide.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Los dos juegos de colores. Compose no lee res/values-night, asi que el modo
// oscuro de esta pantalla se arma aca.
private val LightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = AmberOnPrimary,
    primaryContainer = AmberPrimaryContainer,
    onPrimaryContainer = AmberOnPrimaryContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberPrimaryDark,
    onPrimary = AmberOnPrimaryDark,
    primaryContainer = AmberPrimaryContainerDark,
    onPrimaryContainer = AmberOnPrimaryContainerDark
)

/**
 * Tema de la app para Compose.
 *
 * Envuelve a toda la pantalla de detalle: los componentes de adentro leen de aca
 * los colores y la tipografia con MaterialTheme.colorScheme y
 * MaterialTheme.typography, sin que haya que pasarselos uno por uno.
 *
 * @param darkTheme por defecto sigue al celular, pero se puede forzar. El @Preview
 *                  lo usa para ver la pantalla en claro y en oscuro.
 */
@Composable
fun BreweryGuideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
