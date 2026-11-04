package com.example.parcial2breweryguide.viewmodel

import com.example.parcial2breweryguide.model.BreweryDetails

/**
 * Los tres estados en los que puede estar la pantalla de detalle.
 *
 * Como es una sealed class, el when de la pantalla de Compose esta obligado a
 * dibujar los tres: no se puede dar el caso de que quede la pantalla en blanco
 * porque nos olvidamos de uno.
 */
sealed class DetailUiState {

    /** Todavia se esta buscando la cerveceria (en la base o en la API). */
    object Loading : DetailUiState()

    /** Ya tenemos los datos para mostrar. */
    data class Success(val brewery: BreweryDetails) : DetailUiState()

    /** No se pudo conseguir la cerveceria. El texto es el que se muestra en pantalla. */
    data class Error(val message: String) : DetailUiState()
}
