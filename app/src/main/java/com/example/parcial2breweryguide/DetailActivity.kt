package com.example.parcial2breweryguide

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import com.example.parcial2breweryguide.data.FavoriteRepository
import com.example.parcial2breweryguide.network.NetworkChecker
import com.example.parcial2breweryguide.network.RetrofitClient
import com.example.parcial2breweryguide.repository.BreweryRepository
import com.example.parcial2breweryguide.ui.BreweryDetailScreen
import com.example.parcial2breweryguide.ui.theme.BreweryGuideTheme
import com.example.parcial2breweryguide.viewmodel.BreweryDetailViewModel
import com.example.parcial2breweryguide.viewmodel.BreweryDetailViewModelFactory

/**
 * Pantalla de detalle, la que la consigna pide en Jetpack Compose.
 *
 * Es ComponentActivity y no AppCompatActivity porque no usa ningun layout XML:
 * toda su vista se arma en setContent.
 *
 * Ademas de mostrar la pantalla, se ocupa de los Intent que salen de la app
 * (marcador, navegador y mapa), porque para lanzarlos hace falta una Activity.
 */
class DetailActivity : ComponentActivity() {

    // El id lo necesita la fabrica, asi que se lee antes de armar el ViewModel.
    // Si no llego nada queda vacio y abajo cerramos la pantalla.
    private val breweryId: String by lazy {
        intent.getStringExtra(EXTRA_BREWERY_ID) ?: ""
    }

    private val viewModel: BreweryDetailViewModel by viewModels {
        BreweryDetailViewModelFactory(
            breweryId = breweryId,
            breweryRepository = BreweryRepository(
                apiService = RetrofitClient.breweryApiService,
                networkChecker = NetworkChecker(applicationContext)
            ),
            favoriteRepository = FavoriteRepository(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sin id no hay nada que mostrar, asi que volvemos en vez de dejar una
        // ficha vacia.
        if (breweryId.isEmpty()) {
            finish()
            return
        }

        setContent {
            BreweryGuideTheme {
                BreweryDetailScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onCall = { phone -> dialPhone(phone) },
                    onOpenWebsite = { url -> openWebsite(url) },
                    onOpenMap = { latitude, longitude, name ->
                        openMap(latitude, longitude, name)
                    }
                )
            }
        }
    }

    /**
     * Abre el marcador del celular con el numero ya puesto.
     *
     * ACTION_DIAL no necesita permiso porque no llama sola: deja que el usuario
     * apriete el boton verde. ACTION_CALL si pediria el permiso CALL_PHONE.
     */
    private fun dialPhone(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        startSafely(intent, R.string.error_no_dialer)
    }

    /** Abre el sitio de la cerveceria en el navegador. */
    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startSafely(intent, R.string.error_no_browser)
    }

    /**
     * Abre la cerveceria en la app de mapas.
     *
     * Las coordenadas que van antes del ? centran el mapa, y las de q ponen el
     * marcador con el nombre entre parentesis. Uri.encode escapa los espacios y
     * los acentos del nombre.
     */
    private fun openMap(latitude: Double, longitude: Double, name: String) {
        val label = Uri.encode(name)
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        startSafely(intent, R.string.error_no_maps)
    }

    /**
     * Lanza el Intent y avisa en vez de cerrarse si el celular no tiene ninguna
     * app que sepa abrirlo (por ejemplo una tablet sin marcador).
     */
    private fun startSafely(intent: Intent, @StringRes errorMessageRes: Int) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, errorMessageRes, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Clave del Intent. Va en el companion object para que el que guarda y el
         * que lee usen exactamente el mismo texto.
         *
         * Mandamos solo el id y no la cerveceria entera: asi la pantalla de detalle
         * siempre busca la version mas nueva, y si esta guardada la saca de SQLite.
         */
        const val EXTRA_BREWERY_ID = "extra_brewery_id"
    }
}
