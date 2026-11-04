package com.example.parcial2breweryguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parcial2breweryguide.data.FavoriteRepository
import com.example.parcial2breweryguide.repository.BreweryRepository

/**
 * Fabrica de BreweryDetailViewModel (patron Factory).
 *
 * Esta ademas de los dos repositorios le pasa el id de la cerveceria, que llega
 * por Intent. Darselo en el constructor deja que el ViewModel haga la busqueda en
 * su init, que corre una sola vez aunque la pantalla se recree al rotar.
 */
class BreweryDetailViewModelFactory(
    private val breweryId: String,
    private val breweryRepository: BreweryRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BreweryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BreweryDetailViewModel(
                breweryId,
                breweryRepository,
                favoriteRepository
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
