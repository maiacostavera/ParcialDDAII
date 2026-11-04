package com.example.parcial2breweryguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parcial2breweryguide.data.FavoriteRepository

/**
 * Fabrica de FavoritesViewModel (patron Factory).
 *
 * Igual que BreweryViewModelFactory, pero este ViewModel necesita el repositorio
 * de la base de datos en vez del de la API.
 */
class FavoritesViewModelFactory(
    private val repository: FavoriteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
