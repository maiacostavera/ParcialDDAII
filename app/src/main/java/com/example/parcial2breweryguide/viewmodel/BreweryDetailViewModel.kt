package com.example.parcial2breweryguide.viewmodel

import androidx.lifecycle.ViewModel
import com.example.parcial2breweryguide.data.FavoriteRepository
import com.example.parcial2breweryguide.model.BreweryDetails
import com.example.parcial2breweryguide.repository.BreweryDetailResult
import com.example.parcial2breweryguide.repository.BreweryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel de la pantalla de detalle, la unica hecha en Jetpack Compose.
 *
 * Usa StateFlow en vez de LiveData porque es lo que Compose lee con
 * collectAsState(): el MutableStateFlow es privado y afuera se expone el
 * StateFlow, que es de solo lectura. La idea es la misma que con LiveData.
 *
 * Es el ViewModel el que decide de donde salen los datos: si la cerveceria esta
 * guardada en SQLite los toma de ahi (por eso el detalle anda sin internet), y si
 * no se la pide a la API.
 */
class BreweryDetailViewModel(
    private val breweryId: String,
    private val breweryRepository: BreweryRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    /**
     * El init corre una sola vez, cuando se crea el ViewModel. Al rotar la
     * pantalla Android reusa el mismo, asi que no se vuelve a llamar a la API.
     */
    init {
        loadBrewery()
    }

    private fun loadBrewery() {
        _uiState.value = DetailUiState.Loading

        // Primero la base: si ya esta guardada no hace falta internet.
        val saved = favoriteRepository.getBreweryById(breweryId)
        if (saved != null) {
            _uiState.value = DetailUiState.Success(saved)
            return
        }

        breweryRepository.getBreweryById(breweryId) { result ->
            _uiState.value = when (result) {
                is BreweryDetailResult.Success ->
                    DetailUiState.Success(BreweryDetails.fromBrewery(result.brewery))

                is BreweryDetailResult.NoConnection ->
                    DetailUiState.Error("Sin conexión a internet. Fijate la red y probá de nuevo.")

                is BreweryDetailResult.HttpError ->
                    DetailUiState.Error("Error del servidor (${result.code})")

                is BreweryDetailResult.Unexpected ->
                    DetailUiState.Error("Ocurrió un error inesperado.")
            }
        }
    }

    /**
     * Guarda la cerveceria con el estado elegido, o le cambia el estado si ya
     * estaba guardada.
     *
     * @param status BreweryDetails.STATUS_VISITED o STATUS_PENDING.
     */
    fun saveWithStatus(status: String) {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        if (state.brewery.isSaved()) {
            favoriteRepository.updateStatus(breweryId, status)
        } else {
            favoriteRepository.insertBrewery(state.brewery, status)
        }

        // Releemos de la base para que la pantalla muestre el estado y la fecha
        // que quedaron realmente guardados.
        val saved = favoriteRepository.getBreweryById(breweryId)
        if (saved != null) {
            _uiState.value = DetailUiState.Success(saved)
        }
    }

    /** Saca la cerveceria de la base, pero la pantalla sigue mostrando sus datos. */
    fun removeFromFavorites() {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        favoriteRepository.deleteBrewery(breweryId)

        // copy() crea una copia del data class cambiando solo lo que le decimos.
        _uiState.value = DetailUiState.Success(
            state.brewery.copy(status = null, savedAt = null)
        )
    }
}
