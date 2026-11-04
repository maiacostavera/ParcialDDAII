package com.example.parcial2breweryguide.model

import android.net.Uri
import com.example.parcial2breweryguide.R
import com.google.gson.annotations.SerializedName

/**
 * Una cerveceria tal como la devuelve Open Brewery DB.
 *
 * Casi todos los campos son nulables a proposito: la API no completa lo mismo
 * para todas. En una muestra de 50, 47 traian telefono y 43 sitio web, y las
 * que estan en estado "planning" casi nunca tienen calle cargada.
 */
data class Brewery(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    // micro, brewpub, regional, etc. Se traduce con typeLabelRes().
    @SerializedName("brewery_type")
    val breweryType: String?,

    @SerializedName("street")
    val street: String?,

    @SerializedName("city")
    val city: String?,

    // Provincia o estado. En la API el campo viejo se llama "state".
    @SerializedName("state_province")
    val stateProvince: String?,

    @SerializedName("postal_code")
    val postalCode: String?,

    @SerializedName("country")
    val country: String?,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("website_url")
    val websiteUrl: String?
) {

    /**
     * Junta las partes de la direccion en un solo texto.
     *
     * listOfNotNull descarta las que vienen en null, asi no quedan comas sueltas
     * cuando a la cerveceria le falta la calle o el codigo postal.
     */
    fun getFullAddress(): String {
        return listOfNotNull(street, city, stateProvince, postalCode, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }

    /** URL del icono de esta cerveceria, o null si no tiene sitio web. */
    fun getIconUrl(): String? = iconUrlFor(websiteUrl)

    /** Texto en castellano del tipo de establecimiento. */
    fun getTypeLabelRes(): Int = typeLabelRes(breweryType)

    companion object {

        /**
         * Open Brewery DB no devuelve ninguna imagen, asi que como "imagen de red"
         * usamos el favicon del sitio de cada cerveceria, que lo sirve Google:
         *
         *   https://www.google.com/s2/favicons?domain={dominio}&sz=128
         *
         * Devuelve un PNG, que es justo lo que Picasso necesita. Si el dominio no
         * existe contesta 404 y Picasso muestra la imagen de .error().
         *
         * Esta en el companion object, y no como metodo normal, porque lo usan
         * tanto Brewery (la lista) como BreweryDetails (el detalle y favoritas).
         */
        fun iconUrlFor(websiteUrl: String?): String? {
            if (websiteUrl.isNullOrBlank()) return null

            // Uri.parse separa la URL en partes; host es el dominio solo,
            // sin "http://" ni el resto del camino.
            val host = Uri.parse(websiteUrl).host
            if (host.isNullOrBlank()) return null

            return "https://www.google.com/s2/favicons?domain=$host&sz=128"
        }

        /**
         * Pasa el tipo que manda la API (en ingles) al string en castellano que ve
         * el usuario. Devuelve el id del recurso para que la lista, las favoritas y
         * la pantalla de Compose muestren siempre lo mismo.
         *
         * El else cubre los tipos que la API pueda agregar mas adelante.
         */
        fun typeLabelRes(breweryType: String?): Int = when (breweryType) {
            "micro" -> R.string.type_micro
            "nano" -> R.string.type_nano
            "regional" -> R.string.type_regional
            "brewpub" -> R.string.type_brewpub
            "large" -> R.string.type_large
            "planning" -> R.string.type_planning
            "bar" -> R.string.type_bar
            "contract" -> R.string.type_contract
            "proprietor" -> R.string.type_proprietor
            "closed" -> R.string.type_closed
            "taproom" -> R.string.type_taproom
            "cidery" -> R.string.type_cidery
            "beergarden" -> R.string.type_beergarden
            else -> R.string.type_unknown
        }
    }
}
