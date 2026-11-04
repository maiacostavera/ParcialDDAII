package com.example.parcial2breweryguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parcial2breweryguide.repository.BreweryRepository

/**
 * Fabrica de BreweryViewModel (patron Factory).
 *
 * Hace falta porque BreweryViewModel no tiene constructor vacio: recibe el
 * repositorio. Android no sabe como armarlo solo, entonces le damos esta clase
 * que le explica como.
 *
 * Este es el punto donde se inyectan las dependencias: el Fragment arma el
 * repositorio, se lo pasa a la fabrica, y la fabrica se lo pasa al ViewModel.
 */
class BreweryViewModelFactory(
    private val repository: BreweryRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BreweryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BreweryViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
