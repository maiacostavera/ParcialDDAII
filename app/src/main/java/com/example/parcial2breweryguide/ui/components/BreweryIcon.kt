package com.example.parcial2breweryguide.ui.components

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.example.parcial2breweryguide.R
import com.squareup.picasso.Picasso

/**
 * Muestra el icono de la cerveceria dentro de la pantalla de Compose.
 *
 * Picasso no sabe nada de Compose: su metodo into() espera un ImageView, que es
 * una vista del sistema viejo de XML. AndroidView es el puente que permite meter
 * una de esas vistas adentro de un Composable.
 *
 * factory se ejecuta una sola vez y crea el ImageView; update corre cada vez que
 * cambia algo de lo que el Composable lee (aca, la URL).
 */
@Composable
fun BreweryIcon(iconUrl: String?, modifier: Modifier = Modifier) {

    // En la vista previa de Android Studio no hay red ni se puede usar Picasso,
    // asi que ahi dibujamos el vaso directamente.
    val inPreview = LocalInspectionMode.current

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                // CENTER_INSIDE entra la imagen completa sin deformarla.
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                contentDescription = context.getString(R.string.cd_brewery_icon)
            }
        },
        update = { imageView ->
            if (iconUrl == null || inPreview) {
                // Cortamos cualquier descarga anterior y dejamos el dibujo propio.
                Picasso.get().cancelRequest(imageView)
                imageView.setImageResource(R.drawable.ic_brewery_placeholder)
            } else {
                Picasso.get()
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_brewery_placeholder)
                    .error(R.drawable.ic_brewery_placeholder)
                    .into(imageView)
            }
        }
    )
}
