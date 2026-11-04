package com.example.parcial2breweryguide.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parcial2breweryguide.model.Brewery
import com.example.parcial2breweryguide.repository.BreweryListResult
import com.example.parcial2breweryguide.repository.BreweryRepository

/**
 * ViewModel del listado y el buscador (la "VM" de MVVM).
 *
 * Le pide los datos al repositorio y traduce el resultado a los estados que
 * la pantalla necesita: la lista, si esta cargando, el mensaje y si se puede
 * reintentar. No sabe nada de Retrofit ni de como se consiguen los datos.
 *
 * Al sobrevivir a la rotacion, la busqueda no se pierde si se gira el celular.
 */
class BreweryViewModel(private val repository: BreweryRepository) : ViewModel() {

    // Patron de siempre: el MutableLiveData es privado (solo lo toca el ViewModel)
    // y afuera se expone la version de solo lectura.
    private val _breweries = MutableLiveData<List<Brewery>>()
    val breweries: LiveData<List<Brewery>> get() = _breweries

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Va aparte del mensaje porque "no hubo resultados" tambien muestra un texto,
    // pero ahi no fallo nada y reintentar daria lo mismo.
    private val _canRetry = MutableLiveData<Boolean>()
    val canRetry: LiveData<Boolean> get() = _canRetry

    // Guardamos que fue lo ultimo que se pidio para que el boton Reintentar
    // repita exactamente eso. null significa "el listado general".
    private var lastQuery: String? = null
    private var lastFilterIsCity = true

    /**
     * El init corre cuando se crea el ViewModel, es decir una sola vez por
     * pantalla: al rotar se reusa el mismo y la lista no se vuelve a pedir.
     */
    init {
        loadAllBreweries()
    }

    /** Trae el listado general, sin filtros. */
    fun loadAllBreweries() {
        lastQuery = null
        startLoading()

        repository.getAllBreweries { result ->
            handleResult(result)
        }
    }

    /**
     * Busca con el filtro que eligio el usuario.
     *
     * @param query ciudad o nombre, segun el radio button.
     * @param filterIsCity true para by_city, false para by_name.
     */
    fun searchBreweries(query: String, filterIsCity: Boolean) {
        lastQuery = query
        lastFilterIsCity = filterIsCity
        startLoading()

        if (filterIsCity) {
            repository.searchByCity(query) { result -> handleResult(result) }
        } else {
            repository.searchByName(query) { result -> handleResult(result) }
        }
    }

    /** Vuelve a hacer el ultimo pedido. Lo llama el boton Reintentar. */
    fun retry() {
        val query = lastQuery

        if (query == null) {
            loadAllBreweries()
        } else {
            searchBreweries(query, lastFilterIsCity)
        }
    }

    private fun startLoading() {
        _isLoading.value = true
        _errorMessage.value = null
        _canRetry.value = false
    }

    /** Traduce el resultado del repositorio a los LiveData que mira la pantalla. */
    private fun handleResult(result: BreweryListResult) {
        _isLoading.value = false

        // El when sobre la sealed class obliga a cubrir los cuatro casos.
        when (result) {
            is BreweryListResult.Success -> {
                _breweries.value = result.breweries

                // Anduvo bien pero no hubo coincidencias.
                if (result.breweries.isEmpty()) {
                    _errorMessage.value = "No encontramos cervecerías con esa búsqueda."
                }
            }

            is BreweryListResult.NoConnection ->
                showError("Sin conexión a internet. Fijate la red y probá de nuevo.")

            is BreweryListResult.HttpError ->
                showError("Error del servidor (${result.code})")

            is BreweryListResult.Unexpected ->
                showError("Ocurrió un error inesperado.")
        }
    }

    /** Vacia la lista, muestra el mensaje y ofrece reintentar: los tres errores hacen eso. */
    private fun showError(message: String) {
        _breweries.value = emptyList()
        _errorMessage.value = message
        _canRetry.value = true
    }
}
