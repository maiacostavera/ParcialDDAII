package com.example.parcial2breweryguide.repository

import android.util.Log
import com.example.parcial2breweryguide.model.Brewery
import com.example.parcial2breweryguide.network.BreweryApiService
import com.example.parcial2breweryguide.network.NetworkChecker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * Unica puerta de entrada a los datos que vienen de internet (patron Repository).
 *
 * Concentra dos cosas: chequear que haya red y hablar con la API. Los ViewModels
 * lo llaman y reciben un resultado ya interpretado, asi que no saben que existe
 * Retrofit. Si mañana hubiera que cambiar de API, se toca solo este archivo.
 *
 * Recibe sus dependencias por constructor en vez de crearlas adentro, asi se lo
 * puede armar con un servicio falso para probarlo.
 */
class BreweryRepository(
    private val apiService: BreweryApiService,
    private val networkChecker: NetworkChecker
) {

    /** Listado general, sin filtro. */
    fun getAllBreweries(onResult: (BreweryListResult) -> Unit) {
        if (!networkChecker.isConnected()) {
            Log.w(TAG, "Sin red disponible, no se hace la llamada")
            onResult(BreweryListResult.NoConnection)
            return
        }
        enqueueListCall(apiService.getBreweries(), onResult)
    }

    /** Filtra por ciudad con el parametro by_city de la API. */
    fun searchByCity(city: String, onResult: (BreweryListResult) -> Unit) {
        if (!networkChecker.isConnected()) {
            Log.w(TAG, "Sin red disponible, no se hace la llamada")
            onResult(BreweryListResult.NoConnection)
            return
        }
        enqueueListCall(apiService.getBreweriesByCity(city), onResult)
    }

    /** Filtra por nombre con el parametro by_name de la API. */
    fun searchByName(name: String, onResult: (BreweryListResult) -> Unit) {
        if (!networkChecker.isConnected()) {
            Log.w(TAG, "Sin red disponible, no se hace la llamada")
            onResult(BreweryListResult.NoConnection)
            return
        }
        enqueueListCall(apiService.getBreweriesByName(name), onResult)
    }

    /**
     * Las tres busquedas de arriba cambian solo en la URL, asi que el manejo de la
     * respuesta esta una sola vez aca.
     *
     * enqueue manda la llamada en segundo plano y avisa por los callbacks. Con
     * execute() se colgaria la pantalla.
     */
    private fun enqueueListCall(
        call: Call<List<Brewery>>,
        onResult: (BreweryListResult) -> Unit
    ) {
        call.enqueue(object : Callback<List<Brewery>> {

            override fun onResponse(
                call: Call<List<Brewery>>,
                response: Response<List<Brewery>>
            ) {
                // isSuccessful es true para cualquier codigo 2xx.
                if (response.isSuccessful) {
                    // Cuando no hay coincidencias la API devuelve [], que Gson
                    // convierte en una lista vacia. El ?: cubre el caso raro de
                    // que el cuerpo venga directamente vacio.
                    onResult(BreweryListResult.Success(response.body() ?: emptyList()))
                } else {
                    Log.e(TAG, "El servidor respondio ${response.code()}")
                    onResult(BreweryListResult.HttpError(response.code()))
                }
            }

            override fun onFailure(call: Call<List<Brewery>>, t: Throwable) {
                Log.e(TAG, "Fallo la llamada a la API", t)

                // Una IOException casi siempre es que se corto la conexion o
                // vencio el timeout; cualquier otra cosa es un problema del JSON.
                onResult(
                    if (t is IOException) BreweryListResult.NoConnection
                    else BreweryListResult.Unexpected
                )
            }
        })
    }

    /**
     * Pide UNA cerveceria por su id. Lo usa la pantalla de detalle cuando la
     * cerveceria no esta guardada en la base.
     */
    fun getBreweryById(id: String, onResult: (BreweryDetailResult) -> Unit) {
        if (!networkChecker.isConnected()) {
            Log.w(TAG, "Sin red disponible, no se pide el detalle")
            onResult(BreweryDetailResult.NoConnection)
            return
        }

        apiService.getBreweryById(id).enqueue(object : Callback<Brewery> {

            override fun onResponse(call: Call<Brewery>, response: Response<Brewery>) {
                val brewery = response.body()

                if (response.isSuccessful && brewery != null) {
                    onResult(BreweryDetailResult.Success(brewery))
                } else {
                    Log.e(TAG, "El servidor respondio ${response.code()} al pedir $id")
                    onResult(BreweryDetailResult.HttpError(response.code()))
                }
            }

            override fun onFailure(call: Call<Brewery>, t: Throwable) {
                Log.e(TAG, "Fallo el detalle de $id", t)
                onResult(
                    if (t is IOException) BreweryDetailResult.NoConnection
                    else BreweryDetailResult.Unexpected
                )
            }
        })
    }

    companion object {
        private const val TAG = "BreweryRepository"
    }
}
