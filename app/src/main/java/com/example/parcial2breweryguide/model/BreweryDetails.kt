package com.example.parcial2breweryguide.model

/**
 * Los datos de UNA cerveceria como los muestran la pantalla de detalle y la lista
 * de "Mis cervecerias".
 *
 * Es distinta de Brewery porque aca la direccion ya viene armada en un solo texto
 * (que es como se guarda en SQLite) y ademas estan el estado y la fecha en que se
 * guardo. Tener este modelo aparte deja que el detalle no sepa si los datos
 * salieron de la API o de la base: siempre recibe un BreweryDetails.
 *
 * status y savedAt son nulables: valen null cuando la cerveceria todavia no esta
 * guardada.
 */
data class BreweryDetails(
    val id: String,
    val name: String,
    val breweryType: String?,
    val address: String,
    val city: String?,
    val country: String?,
    val phone: String?,
    val websiteUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: String?,
    val savedAt: String?
) {

    /** true si la cerveceria ya esta guardada en la base. */
    fun isSaved(): Boolean = status != null

    /** URL del icono, o null si no tiene sitio web. */
    fun getIconUrl(): String? = Brewery.iconUrlFor(websiteUrl)

    /** Texto en castellano del tipo de establecimiento. */
    fun getTypeLabelRes(): Int = Brewery.typeLabelRes(breweryType)

    companion object {

        /** Estados posibles. Se guardan en ingles porque van a una columna de la base. */
        const val STATUS_VISITED = "visited"
        const val STATUS_PENDING = "pending"

        /**
         * Convierte lo que vino de la API en el modelo que usa la pantalla.
         * Como todavia no esta guardada, status y savedAt quedan en null.
         */
        fun fromBrewery(brewery: Brewery): BreweryDetails {
            return BreweryDetails(
                id = brewery.id,
                name = brewery.name,
                breweryType = brewery.breweryType,
                address = brewery.getFullAddress(),
                city = brewery.city,
                country = brewery.country,
                phone = brewery.phone,
                websiteUrl = brewery.websiteUrl,
                latitude = brewery.latitude,
                longitude = brewery.longitude,
                status = null,
                savedAt = null
            )
        }
    }
}
