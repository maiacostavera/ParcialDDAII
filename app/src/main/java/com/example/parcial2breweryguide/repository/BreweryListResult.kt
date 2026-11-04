package com.example.parcial2breweryguide.repository

import com.example.parcial2breweryguide.model.Brewery

/**
 * Los cuatro finales posibles de un pedido de listado (todas, por ciudad o por nombre).
 *
 * Es el contrato que el repositorio le devuelve al ViewModel. Al ser una sealed
 * class, el compilador obliga a contemplar todos los casos en el when: si mañana
 * apareciera uno nuevo, no se puede olvidar de manejarlo.
 */
sealed class BreweryListResult {

    /** La API contesto bien. La lista puede venir vacia si no hubo coincidencias. */
    data class Success(val breweries: List<Brewery>) : BreweryListResult()

    /** No hay red, o se corto en el medio de la llamada. */
    object NoConnection : BreweryListResult()

    /** El servidor contesto con un codigo de error (404, 500, etc). */
    data class HttpError(val code: Int) : BreweryListResult()

    /** Cualquier otra cosa, normalmente un problema al leer el JSON. */
    object Unexpected : BreweryListResult()
}
