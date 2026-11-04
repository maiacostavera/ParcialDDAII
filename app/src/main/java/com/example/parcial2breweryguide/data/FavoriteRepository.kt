package com.example.parcial2breweryguide.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.parcial2breweryguide.model.BreweryDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Unica puerta de entrada a las cervecerias guardadas (patron Repository, igual
 * que BreweryRepository pero contra SQLite en vez de contra la API).
 *
 * Tiene las cuatro operaciones: guardar, leer, cambiar el estado y borrar.
 * Los ViewModels le piden BreweryDetails y nunca ven un Cursor.
 */
class FavoriteRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    /**
     * Guarda una cerveceria nueva con el estado que eligio el usuario.
     *
     * @return el id de la fila insertada, o -1 si fallo.
     */
    fun insertBrewery(brewery: BreweryDetails, status: String): Long {
        // writableDatabase abre la base para escribir (y la crea si es la primera vez).
        val db = dbHelper.writableDatabase

        // ContentValues es un mapa de columna a valor. Se usa en vez de escribir
        // el INSERT a mano, asi Android arma el SQL y escapa los valores.
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, brewery.id)
            put(DatabaseHelper.COLUMN_NAME, brewery.name)
            put(DatabaseHelper.COLUMN_TYPE, brewery.breweryType)
            put(DatabaseHelper.COLUMN_ADDRESS, brewery.address)
            put(DatabaseHelper.COLUMN_CITY, brewery.city)
            put(DatabaseHelper.COLUMN_COUNTRY, brewery.country)
            put(DatabaseHelper.COLUMN_PHONE, brewery.phone)
            put(DatabaseHelper.COLUMN_WEBSITE, brewery.websiteUrl)
            put(DatabaseHelper.COLUMN_LATITUDE, brewery.latitude)
            put(DatabaseHelper.COLUMN_LONGITUDE, brewery.longitude)
            put(DatabaseHelper.COLUMN_STATUS, status)
            put(DatabaseHelper.COLUMN_SAVED_AT, today())
        }

        val newRowId = db.insert(DatabaseHelper.TABLE_BREWERIES, null, values)
        db.close()

        if (newRowId == -1L) {
            Log.e(TAG, "No se pudo guardar la cerveceria ${brewery.id}")
        }
        return newRowId
    }

    /** Todas las cervecerias con ese estado, de la ultima guardada a la primera. */
    fun getBreweriesByStatus(status: String): List<BreweryDetails> {
        val breweryList = mutableListOf<BreweryDetails>()

        // readableDatabase alcanza porque solo vamos a leer.
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_BREWERIES,
            null, // columns: null devuelve todas las columnas de la tabla (*)
            "${DatabaseHelper.COLUMN_STATUS} = ?", // selection: la clausula WHERE
            arrayOf(status), // selectionArgs: lo que reemplaza al ?
            null, // groupBy: clausula GROUP BY
            null, // having: clausula HAVING
            "${DatabaseHelper.COLUMN_SAVED_AT} DESC" // orderBy: las ultimas primero
        )

        // use cierra el cursor al terminar, aunque el bloque tire una excepcion.
        cursor.use { c ->
            while (c.moveToNext()) {
                breweryList.add(readBrewery(c))
            }
        }

        db.close()
        return breweryList
    }

    /**
     * Busca una cerveceria por su id.
     *
     * @return la cerveceria guardada, o null si no esta en la base.
     */
    fun getBreweryById(id: String): BreweryDetails? {
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_BREWERIES,
            null,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        // Como id es la clave primaria, o viene una fila o no viene ninguna.
        var brewery: BreweryDetails? = null
        cursor.use { c ->
            if (c.moveToFirst()) {
                brewery = readBrewery(c)
            }
        }

        db.close()
        return brewery
    }

    /**
     * Cambia el estado de una cerveceria ya guardada.
     *
     * @return cuantas filas se modificaron (1 si existia, 0 si no).
     */
    fun updateStatus(id: String, status: String): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_STATUS, status)
            // Al cambiarla de estado vuelve a quedar arriba de la lista.
            put(DatabaseHelper.COLUMN_SAVED_AT, today())
        }

        val rowsAffected = db.update(
            DatabaseHelper.TABLE_BREWERIES,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?", // whereClause
            arrayOf(id) // whereArgs
        )

        db.close()
        return rowsAffected
    }

    /**
     * Saca la cerveceria de la base.
     *
     * @return cuantas filas se borraron.
     */
    fun deleteBrewery(id: String): Int {
        val db = dbHelper.writableDatabase

        val rowsDeleted = db.delete(
            DatabaseHelper.TABLE_BREWERIES,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id)
        )

        db.close()
        return rowsDeleted
    }

    /**
     * Pasa la fila en la que esta parado el cursor a un BreweryDetails.
     *
     * Primero se pide el indice (la posicion numerica) de cada columna y recien
     * despues se lee el valor segun su tipo. getColumnIndexOrThrow avisa con una
     * excepcion si la columna no existe, en vez de devolver -1 en silencio.
     */
    private fun readBrewery(c: Cursor): BreweryDetails {
        val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)
        val nameIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)
        val typeIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)
        val addressIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS)
        val cityIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CITY)
        val countryIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COUNTRY)
        val phoneIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)
        val websiteIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEBSITE)
        val latitudeIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE)
        val longitudeIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE)
        val statusIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)
        val savedAtIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SAVED_AT)

        return BreweryDetails(
            id = c.getString(idIndex),
            name = c.getString(nameIndex),
            breweryType = c.getString(typeIndex),
            address = c.getString(addressIndex) ?: "",
            city = c.getString(cityIndex),
            country = c.getString(countryIndex),
            phone = c.getString(phoneIndex),
            websiteUrl = c.getString(websiteIndex),
            // getDouble devuelve 0.0 cuando la columna esta vacia, y 0.0 es una
            // coordenada valida. Por eso preguntamos antes si es null.
            latitude = if (c.isNull(latitudeIndex)) null else c.getDouble(latitudeIndex),
            longitude = if (c.isNull(longitudeIndex)) null else c.getDouble(longitudeIndex),
            status = c.getString(statusIndex),
            savedAt = c.getString(savedAtIndex)
        )
    }

    /** Fecha de hoy como texto, para la columna saved_at. */
    private fun today(): String {
        // El formato ano-mes-dia ordena bien alfabeticamente, que es como lo
        // compara SQLite en el ORDER BY.
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Date())
    }

    companion object {
        private const val TAG = "FavoriteRepository"
    }
}
