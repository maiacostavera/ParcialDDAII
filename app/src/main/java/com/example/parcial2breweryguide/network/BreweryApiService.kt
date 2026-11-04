package com.example.parcial2breweryguide.network

import com.example.parcial2breweryguide.model.Brewery
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de Open Brewery DB que usa la app.
 *
 * Retrofit toma esta interfaz y genera solo la implementacion que arma la URL y
 * hace la llamada HTTP.
 *
 * Los tres devuelven Call<...>: la llamada queda preparada pero no sale hasta que
 * el repositorio le hace enqueue().
 */
interface BreweryApiService {

    /** Listado general, sin filtrar. */
    @GET("v1/breweries")
    fun getBreweries(
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): Call<List<Brewery>>

    /**
     * Filtra por ciudad.
     *
     * @param city lo que escribio el usuario. Los espacios no son problema:
     *             Retrofit codifica el valor antes de pegarlo en la URL.
     */
    @GET("v1/breweries")
    fun getBreweriesByCity(
        @Query("by_city") city: String,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): Call<List<Brewery>>

    /** Filtra por nombre de la cerveceria. */
    @GET("v1/breweries")
    fun getBreweriesByName(
        @Query("by_name") name: String,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): Call<List<Brewery>>

    /**
     * Una sola cerveceria por su id.
     *
     * @Path reemplaza el {id} de la ruta por el valor que se le pasa, a diferencia
     * de @Query que agrega algo despues del signo de pregunta.
     */
    @GET("v1/breweries/{id}")
    fun getBreweryById(
        @Path("id") id: String
    ): Call<Brewery>

    companion object {
        // La API acepta hasta 200 por pagina; con 50 alcanza para la pantalla.
        const val DEFAULT_PER_PAGE = 50
    }
}
