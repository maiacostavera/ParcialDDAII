package com.example.parcial2breweryguide.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Unico lugar donde se configura la conexion a la API.
 *
 * Es un object (singleton) para no crear un Retrofit nuevo en cada llamada: armarlo
 * es caro y ademas asi todas las pantallas comparten el mismo pool de conexiones.
 * El "by lazy" hace que se construya recien la primera vez que se usa.
 */
object RetrofitClient {

    // La barra final es obligatoria: sin ella Retrofit tira una excepcion al armarse.
    private const val BASE_URL = "https://api.openbrewerydb.org/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // GsonConverterFactory es el que convierte el JSON en nuestras data classes.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val breweryApiService: BreweryApiService by lazy {
        retrofit.create(BreweryApiService::class.java)
    }
}
