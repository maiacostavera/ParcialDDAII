package com.example.parcial2breweryguide.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parcial2breweryguide.data.FavoriteRepository
import com.example.parcial2breweryguide.model.BreweryDetails

/**
 * ViewModel de "Mis cervecerias".
 *
 * Trabaja solo contra SQLite, asi que esta pantalla anda igual sin internet.
 * Guarda tambien cual de las dos pestañas esta elegida, para que al rotar el
 * celular no vuelva sola a "Visitadas".
 */
class FavoritesViewModel(private val repository: FavoriteRepository) : ViewModel() {

    private val _favorites = MutableLiveData<List<BreweryDetails>>()
    val favorites: LiveData<List<BreweryDetails>> get() = _favorites

    /** Pestaña elegida. Arranca en "Visitadas", que es la primera. */
    var selectedStatus: String = BreweryDetails.STATUS_VISITED
        private set

    /** Cambia de pestaña y recarga la lista. */
    fun selectStatus(status: String) {
        selectedStatus = status
        loadFavorites()
    }

    /**
     * Relee la base y publica el resultado.
     *
     * Se llama tambien desde onResume del Fragment, para que al volver del detalle
     * se vea lo que se haya guardado o cambiado alla.
     */
    fun loadFavorites() {
        _favorites.value = repository.getBreweriesByStatus(selectedStatus)
    }

    /** Pasa una cerveceria de visitada a pendiente, o al reves. */
    fun toggleStatus(brewery: BreweryDetails) {
        val newStatus = if (brewery.status == BreweryDetails.STATUS_VISITED) {
            BreweryDetails.STATUS_PENDING
        } else {
            BreweryDetails.STATUS_VISITED
        }

        repository.updateStatus(brewery.id, newStatus)

        // Como cambio de estado, ya no pertenece a la pestaña que se esta viendo:
        // recargamos para que desaparezca de la lista.
        loadFavorites()
    }

    /** Borra la cerveceria de la base. El Fragment ya pidio confirmacion. */
    fun deleteBrewery(brewery: BreweryDetails) {
        repository.deleteBrewery(brewery.id)
        loadFavorites()
    }
}
