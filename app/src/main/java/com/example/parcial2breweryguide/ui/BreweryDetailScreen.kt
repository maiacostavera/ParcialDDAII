package com.example.parcial2breweryguide.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parcial2breweryguide.R
import com.example.parcial2breweryguide.model.BreweryDetails
import com.example.parcial2breweryguide.ui.components.ActionButton
import com.example.parcial2breweryguide.ui.components.BreweryIcon
import com.example.parcial2breweryguide.ui.components.InfoRow
import com.example.parcial2breweryguide.ui.components.SaveStatusSection
import com.example.parcial2breweryguide.ui.theme.BreweryGuideTheme
import com.example.parcial2breweryguide.viewmodel.BreweryDetailViewModel
import com.example.parcial2breweryguide.viewmodel.DetailUiState

/**
 * Pantalla de detalle completa: la barra de arriba y, debajo, lo que corresponda
 * segun el estado del ViewModel.
 *
 * Es la unica que habla con el ViewModel. Las acciones que salen de la app
 * (llamar, abrir el navegador, abrir el mapa) llegan como lambdas desde
 * DetailActivity, porque para lanzar un Intent hace falta la Activity.
 */
// TopAppBar todavia esta marcada como experimental en Material 3, y el
// compilador pide que digamos que lo sabemos.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreweryDetailScreen(
    viewModel: BreweryDetailViewModel,
    onBack: () -> Unit,
    onCall: (String) -> Unit,
    onOpenWebsite: (String) -> Unit,
    onOpenMap: (Double, Double, String) -> Unit
) {
    // collectAsState se suscribe al StateFlow del ViewModel, igual que un observer
    // de LiveData. El "by" deja usar uiState como si fuera el valor directamente.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // El when sobre la sealed class cubre los tres estados posibles.
        when (val state = uiState) {
            is DetailUiState.Loading -> CenteredBox(Modifier.padding(innerPadding)) {
                CircularProgressIndicator()
            }

            is DetailUiState.Error -> CenteredBox(Modifier.padding(innerPadding)) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            is DetailUiState.Success -> BreweryDetailContent(
                brewery = state.brewery,
                onCall = onCall,
                onOpenWebsite = onOpenWebsite,
                onOpenMap = onOpenMap,
                onSave = { status -> viewModel.saveWithStatus(status) },
                onRemove = { viewModel.removeFromFavorites() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * La ficha propiamente dicha.
 *
 * No conoce el ViewModel: recibe los datos ya armados y avisa por lambdas. Por eso
 * el @Preview de abajo la puede dibujar con una cerveceria inventada.
 */
@Composable
fun BreweryDetailContent(
    brewery: BreweryDetails,
    onCall: (String) -> Unit,
    onOpenWebsite: (String) -> Unit,
    onOpenMap: (Double, Double, String) -> Unit,
    onSave: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Guardamos en variables locales los datos que pueden faltar. Asi se chequean
    // una sola vez y adentro de los if Kotlin ya sabe que no son null.
    val phone = brewery.phone
    val websiteUrl = brewery.websiteUrl
    val latitude = brewery.latitude
    val longitude = brewery.longitude

    Column(
        modifier = modifier
            .fillMaxSize()
            // rememberScrollState guarda cuanto se bajo la pantalla; sin el
            // remember se volveria al principio en cada redibujado.
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Encabezado: icono, nombre y tipo.
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BreweryIcon(
                    iconUrl = brewery.getIconUrl(),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = brewery.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(brewery.getTypeLabelRes()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Datos de contacto. Cada uno aparece solo si la API lo trajo.
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(
                    label = stringResource(R.string.label_address),
                    value = brewery.address.ifEmpty { stringResource(R.string.not_available) }
                )

                if (!phone.isNullOrBlank()) {
                    InfoRow(
                        label = stringResource(R.string.label_phone),
                        value = phone
                    )
                }

                if (!websiteUrl.isNullOrBlank()) {
                    InfoRow(
                        label = stringResource(R.string.label_website),
                        value = websiteUrl
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones que abren otras apps del celular. Se esconden si falta el dato.
        if (!phone.isNullOrBlank()) {
            ActionButton(
                iconRes = R.drawable.ic_phone,
                text = stringResource(R.string.button_call),
                onClick = { onCall(phone) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!websiteUrl.isNullOrBlank()) {
            ActionButton(
                iconRes = R.drawable.ic_web,
                text = stringResource(R.string.button_website),
                onClick = { onOpenWebsite(websiteUrl) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // El mapa necesita las dos coordenadas, por eso se piden juntas.
        if (latitude != null && longitude != null) {
            ActionButton(
                iconRes = R.drawable.ic_map,
                text = stringResource(R.string.button_map),
                onClick = { onOpenMap(latitude, longitude, brewery.name) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        SaveStatusSection(
            status = brewery.status,
            onSave = onSave,
            onRemove = onRemove
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** Caja que centra lo que tenga adentro. La usan el cargando y el error. */
@Composable
private fun CenteredBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Vista previa de Android Studio, con una cerveceria inventada.
 *
 * Sirve para ver como queda la ficha sin tener que compilar e instalar la app.
 * Las lambdas van vacias porque en la vista previa no hay nada que abrir.
 */
@Preview(showBackground = true)
@Composable
fun BreweryDetailContentPreview() {
    val sampleBrewery = BreweryDetails(
        id = "ejemplo-1",
        name = "Patagonia Brewing",
        breweryType = "brewpub",
        address = "Av. Bustillo 11.500, Bariloche, Río Negro, Argentina",
        city = "Bariloche",
        country = "Argentina",
        phone = "+54 294 445 8600",
        websiteUrl = "https://www.cervezapatagonia.com.ar",
        latitude = -41.1156,
        longitude = -71.4256,
        status = BreweryDetails.STATUS_PENDING,
        savedAt = "2026-11-04"
    )

    BreweryGuideTheme {
        BreweryDetailContent(
            brewery = sampleBrewery,
            onCall = {},
            onOpenWebsite = {},
            onOpenMap = { _, _, _ -> },
            onSave = {},
            onRemove = {}
        )
    }
}
