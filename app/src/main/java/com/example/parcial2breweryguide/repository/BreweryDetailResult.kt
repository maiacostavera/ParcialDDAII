package com.example.parcial2breweryguide.repository

import com.example.parcial2breweryguide.model.Brewery

/**
 * Lo mismo que BreweryListResult pero para cuando se pide UNA cerveceria por id.
 *
 * Va aparte porque el Success de aca trae un solo Brewery y no una lista, y asi
 * la pantalla de detalle no tiene que preguntarse si la lista vino vacia.
 */
sealed class BreweryDetailResult {

    /** La API encontro la cerveceria. */
    data class Success(val brewery: Brewery) : BreweryDetailResult()

    /** No hay red, o se corto en el medio de la llamada. */
    object NoConnection : BreweryDetailResult()

    /** El servidor contesto con un codigo de error (por ejemplo 404 si el id no existe). */
    data class HttpError(val code: Int) : BreweryDetailResult()

    /** Cualquier otra cosa, normalmente un problema al leer el JSON. */
    object Unexpected : BreweryDetailResult()
}
