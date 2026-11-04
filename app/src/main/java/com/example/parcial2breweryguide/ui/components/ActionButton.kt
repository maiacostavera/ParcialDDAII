package com.example.parcial2breweryguide.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Boton de accion de la ficha (llamar, abrir el sitio, ver el mapa).
 *
 * Los tres se ven igual y solo cambian el icono, el texto y lo que hacen, asi que
 * se escribe una vez sola.
 *
 * @param iconRes vector de res/drawable. En Compose se carga con painterResource.
 */
@Composable
fun ActionButton(
    @DrawableRes iconRes: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(iconRes),
            // null porque el texto del boton ya dice lo mismo y el lector de
            // pantalla lo repetiria dos veces.
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}
