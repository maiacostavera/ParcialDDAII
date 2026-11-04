package com.example.parcial2breweryguide.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.parcial2breweryguide.R
import com.example.parcial2breweryguide.model.BreweryDetails

/**
 * La parte de abajo de la ficha: guardar la cerveceria en SQLite o cambiarle el
 * estado.
 *
 * Muestra dos cosas distintas segun el estado que recibe:
 * - si es null la cerveceria no esta guardada, y se ofrecen los dos botones;
 * - si ya esta guardada, se dice como esta y se ofrece pasarla al otro estado o
 *   sacarla de la lista.
 *
 * @param status BreweryDetails.STATUS_VISITED, STATUS_PENDING o null.
 */
@Composable
fun SaveStatusSection(
    status: String?,
    onSave: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (status == null) {
                Text(
                    text = stringResource(R.string.label_save_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onSave(BreweryDetails.STATUS_VISITED) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.button_mark_visited))
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onSave(BreweryDetails.STATUS_PENDING) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.button_mark_pending))
                }
            } else {
                val isVisited = status == BreweryDetails.STATUS_VISITED

                Text(
                    text = stringResource(R.string.label_saved_as),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        if (isVisited) R.string.status_visited else R.string.status_pending
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                // El boton ofrece siempre el estado contrario al que tiene ahora.
                Button(
                    onClick = {
                        onSave(
                            if (isVisited) BreweryDetails.STATUS_PENDING
                            else BreweryDetails.STATUS_VISITED
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            if (isVisited) R.string.button_mark_pending
                            else R.string.button_mark_visited
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.button_remove_favorite))
                }
            }
        }
    }
}
