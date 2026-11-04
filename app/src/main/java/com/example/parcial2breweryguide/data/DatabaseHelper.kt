package com.example.parcial2breweryguide.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Crea y mantiene la base de datos SQLite de la app.
 *
 * SQLiteOpenHelper se encarga de abrir el archivo de la base, y de llamar a
 * onCreate la primera vez o a onUpgrade cuando sube DATABASE_VERSION. Nosotros
 * solo escribimos el SQL.
 *
 * Los nombres de la tabla y de las columnas son constantes del companion object
 * para que el repositorio los use siempre igual y no haya errores de tipeo.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null, // factory: no hace falta un cursor propio, se usa el que trae Android
    DATABASE_VERSION
) {

    /** Se ejecuta una sola vez, cuando el archivo de la base todavia no existe. */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_BREWERIES)
    }

    /**
     * Se ejecuta cuando DATABASE_VERSION es mayor que la version guardada.
     *
     * Borramos y volvemos a crear porque son cervecerias que el usuario puede
     * guardar de nuevo; si fueran datos que no se recuperan habria que migrarlos.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BREWERIES")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "breweries.db"
        private const val DATABASE_VERSION = 1

        /** Nombre de la tabla de cervecerias guardadas. */
        const val TABLE_BREWERIES = "breweries"

        // El id es el mismo que manda la API, por eso es TEXT y no un entero.
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_TYPE = "brewery_type"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_CITY = "city"
        const val COLUMN_COUNTRY = "country"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_WEBSITE = "website_url"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_STATUS = "status"
        const val COLUMN_SAVED_AT = "saved_at"

        /**
         * El id de la API es la clave primaria, asi que la misma cerveceria no se
         * puede guardar dos veces.
         *
         * Las columnas que pueden faltar (telefono, sitio web, coordenadas) van sin
         * NOT NULL porque la API no las completa para todas.
         */
        private const val CREATE_TABLE_BREWERIES = """
            CREATE TABLE $TABLE_BREWERIES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_TYPE TEXT,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_CITY TEXT,
                $COLUMN_COUNTRY TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_WEBSITE TEXT,
                $COLUMN_LATITUDE REAL,
                $COLUMN_LONGITUDE REAL,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_SAVED_AT TEXT NOT NULL
            )
        """
    }
}
