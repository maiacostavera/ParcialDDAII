# Parcial 2 — BreweryGuide

Aplicación Android en Kotlin que permite explorar cervecerías y productores
artesanales de todo el mundo usando la **Open Brewery DB**, buscarlas por ciudad o
por nombre, ver su ficha completa y guardarlas como **Visitadas** o **Pendientes
por visitar** para consultarlas sin conexión.

Segundo examen de Desarrollo de Aplicaciones Android con Kotlin — **Tema 3**
Mailen Acosta Vera

## Video demostrativo

**[Ver video](PEGAR_ACA_EL_LINK)**

## La API

Se usa Open Brewery DB, que es pública y gratuita (no hace falta API key):

```
GET https://api.openbrewerydb.org/v1/breweries?per_page=50
GET https://api.openbrewerydb.org/v1/breweries?by_city=san diego&per_page=50
GET https://api.openbrewerydb.org/v1/breweries?by_name=dog&per_page=50
GET https://api.openbrewerydb.org/v1/breweries/{id}
```

De cada cervecería la app usa el `id`, el `name`, el `brewery_type`, las partes de
la dirección (`street`, `city`, `state_province`, `postal_code`, `country`), las
coordenadas, el `phone` y el `website_url`.

Dos cosas de la API condicionaron el código:

- **Una búsqueda sin resultados devuelve una lista vacía `[]`**, no un error. Por eso
  `Success` con la lista vacía es un caso aparte en el ViewModel, que muestra el
  mensaje "No encontramos cervecerías con esa búsqueda".
- **`phone` y `website_url` vienen en `null` bastante seguido** (en una muestra de 50,
  47 tenían teléfono, 43 sitio web y 39 coordenadas), y las cervecerías en estado `planning`
  casi nunca tienen calle (a 49 de 50 les faltaba). Casi todos los campos del modelo
  son nulables y cada dato que falta se oculta en vez de mostrarse vacío.

Los tipos de establecimiento llegan en inglés (`micro`, `brewpub`, `regional`, …) y
se pasan a castellano con un `when` en `Brewery.typeLabelRes()`.

### Por qué los íconos salen de Google

El requisito 7 pide usar **Picasso** para descargar imágenes de red, pero Open
Brewery DB no devuelve ninguna imagen: no hay fotos ni logos en la respuesta.

La solución fue usar como imagen el **ícono del sitio web** de cada cervecería, que
sirve el servicio de favicons de Google:

```
https://www.google.com/s2/favicons?domain={dominio-del-website_url}&sz=128
```

Devuelve un PNG, que es exactamente lo que Picasso necesita. El dominio
se saca del `website_url` con `Uri.parse(...).host`, en `Brewery.iconUrlFor()`.

Cuando la cervecería no tiene sitio web, o el dominio ya no existe y el servicio
contesta 404, se muestra `ic_brewery_placeholder.xml`, un vector propio de un vaso
de cerveza. Eso se resuelve con `.placeholder(...)` y `.error(...)` de Picasso.

## Pantallas

**1. Listado y buscador (`MainActivity` + `BreweryListFragment`)**
La Activity tiene la `MaterialToolbar` y un contenedor de Fragments; el listado en
sí es un Fragment con un `RecyclerView`. Arriba hay un campo de texto, dos
`RadioButton` para elegir si se busca por **Ciudad** (`by_city`) o por **Nombre**
(`by_name`), el botón **Buscar** y un botón **Ver todas** que vuelve al listado
general. Cada fila muestra el ícono, el nombre, el tipo en castellano y la ciudad.
Mientras carga hay un `ProgressBar`, y si algo falla aparece el mensaje con un
botón **Reintentar**.

**2. Detalle (`DetailActivity`, en Jetpack Compose)**
Se abre al tocar una cervecería y recibe solo el `id` por el `Intent`. Si esa
cervecería está guardada en SQLite los datos salen de la base, así que la ficha
funciona sin internet; si no, se la pide a la API por id. Muestra el ícono, el
nombre, el tipo, la dirección completa, el teléfono y el sitio web, y tiene tres
botones que abren otras apps del celular: **Llamar**, **Visitar sitio web** y
**Ver en el mapa**. Cada uno aparece solo si el dato existe. Abajo están los
botones para guardarla como visitada o pendiente, cambiarle el estado o sacarla de
la lista.

**3. Mis cervecerías (`FavoritesFragment`)**
Las cervecerías guardadas en SQLite, con dos pestañas (`TabLayout`): **Visitadas** y
**Pendientes**. Desde cada fila se puede cambiar el estado o borrarla, y borrar pide
confirmación con un `AlertDialog`. No toca la API, así que anda sin internet. Si la
pestaña está vacía, explica cómo guardar una cervecería.

Se llega desde el ícono de la barra de arriba. Esa transacción va al back stack, así
que el botón atrás del celular vuelve al listado.

## Arquitectura

La app sigue **MVVM (Model - View - ViewModel)** con **Repository Pattern**:

```
app/src/main/java/com/example/parcial2breweryguide/
├── model/
│   ├── Brewery.kt                      Cervecería como la devuelve la API
│   └── BreweryDetails.kt               Cervecería como la muestran el detalle y SQLite
├── network/
│   ├── BreweryApiService.kt            Endpoints de Retrofit
│   ├── RetrofitClient.kt               Configuración de la conexión (singleton)
│   └── NetworkChecker.kt               Consulta si hay red disponible
├── repository/
│   ├── BreweryRepository.kt            Única puerta de entrada a los datos de red
│   ├── BreweryListResult.kt            Los 4 resultados posibles de un listado
│   └── BreweryDetailResult.kt          Los 4 resultados posibles de pedir una sola
├── data/
│   ├── DatabaseHelper.kt               SQLiteOpenHelper: crea la tabla breweries
│   └── FavoriteRepository.kt           Guardar, leer, cambiar estado y borrar
├── adapter/
│   ├── BreweryAdapter.kt               Adapter del listado + su ViewHolder
│   └── FavoriteAdapter.kt              Adapter de Mis cervecerías + su ViewHolder
├── viewmodel/
│   ├── BreweryViewModel.kt             Estados del listado (LiveData)
│   ├── BreweryViewModelFactory.kt
│   ├── FavoritesViewModel.kt           Estados de Mis cervecerías (LiveData)
│   ├── FavoritesViewModelFactory.kt
│   ├── BreweryDetailViewModel.kt       Estado del detalle (StateFlow)
│   ├── BreweryDetailViewModelFactory.kt
│   └── DetailUiState.kt                Loading / Success / Error del detalle
├── ui/
│   ├── BreweryDetailScreen.kt          Pantalla de detalle en Compose + @Preview
│   ├── components/
│   │   ├── BreweryIcon.kt              Picasso adentro de un AndroidView
│   │   ├── InfoRow.kt                  Etiqueta + valor
│   │   ├── ActionButton.kt             Botón con ícono
│   │   └── SaveStatusSection.kt        Guardar / cambiar estado / quitar
│   └── theme/
│       ├── Color.kt                    Paleta ámbar
│       ├── Type.kt                     Tipografía
│       └── Theme.kt                    BreweryGuideTheme (claro y oscuro)
├── MainActivity.kt                     Toolbar + contenedor de Fragments
├── BreweryListFragment.kt              Pantalla 1: buscador y listado
├── FavoritesFragment.kt                Pantalla 3: Mis cervecerías
└── DetailActivity.kt                   Pantalla 2: hospeda la pantalla de Compose
```

El flujo de una búsqueda es siempre el mismo:

```
BreweryListFragment → BreweryViewModel → BreweryRepository → NetworkChecker + Retrofit
          ↑                                        │
          └────────────── BreweryListResult ───────┘
```

Y el de la pantalla de detalle:

```
DetailActivity → BreweryDetailViewModel → FavoriteRepository (SQLite)  ← primero
                                        → BreweryRepository (API)      ← si no está guardada
```

- **Model**: las data classes. Solo datos y un par de funciones de formato.
- **View** (Activities, Fragments, Adapters y los Composables): muestran datos y
  escuchan toques. No saben que existen Retrofit ni SQLite.
- **ViewModel**: traduce lo que devuelven los repositorios a los estados que la
  pantalla observa. Como sobrevive a la rotación, los resultados no se pierden al
  girar el celular.
- **Repository**: los únicos que hablan con la red y con la base.

## Decisiones clave

### Por qué hay dos modelos

`Brewery` es lo que devuelve la API, con la dirección repartida en cinco campos.
`BreweryDetails` es lo que muestran la ficha y Mis cervecerías: ahí la dirección ya
está armada en un solo texto (que es como se guarda en SQLite) y además están el
estado y la fecha.

Gracias a eso la pantalla de detalle **no sabe de dónde salieron los datos**: recibe
siempre un `BreweryDetails`, haya venido de la base o de la API. La conversión está
en un solo lugar, `BreweryDetails.fromBrewery()`.

### Por qué el detalle recibe solo el id

`DetailActivity` recibe por `Intent` únicamente el `id` de la cervecería. Así la
pantalla siempre busca la versión más nueva: primero mira SQLite (y si está, la
ficha anda sin internet) y recién si no está sale a la API. Si se mandara la
cervecería entera serializada, el detalle mostraría una copia vieja y no sabría si
está guardada.

### Por qué el detalle usa StateFlow y las otras pantallas LiveData

Las pantallas de XML observan `LiveData`, que es lo que se vio en clase. La de
Compose usa `StateFlow` porque es lo que Compose lee con `collectAsState()`. La idea
es la misma: el mutable es privado y afuera se expone la versión de solo lectura.

### Por qué Picasso va adentro de un AndroidView

Picasso trabaja con vistas del sistema viejo: su método `into()` espera un
`ImageView`. En Compose no hay `ImageView`, así que se usa `AndroidView`, que es el
puente que permite meter una vista de XML adentro de un Composable. Está en
`ui/components/BreweryIcon.kt`.

### Por qué la búsqueda es con botón y no en tiempo real

Buscando a medida que se escribe se dispararía una llamada por cada letra. Con el
botón sale una sola llamada, cuando el usuario ya terminó de escribir.

## Manejo de errores

| Situación | Qué hace la app |
|---|---|
| Buscando | Muestra un `ProgressBar` y deshabilita Buscar y Ver todas |
| Sin red (modo avión, sin datos) | `NetworkChecker` corta antes de llamar y avisa al instante |
| Se corta la conexión en el medio | `IOException` en `onFailure` → mismo mensaje |
| Error HTTP (404, 500, etc.) | `response.isSuccessful` es false → mensaje con el código |
| Error al leer el JSON | `onFailure` con otra excepción → "Ocurrió un error inesperado" |
| Búsqueda sin resultados (lista vacía) | Mensaje "No encontramos cervecerías con esa búsqueda", sin Reintentar porque no falló nada |
| Campo de búsqueda vacío | Toast pidiendo que escriba algo |
| Sin conexión, error HTTP o inesperado | Botón **Reintentar**, que repite el último pedido |
| Cervecería sin teléfono / sitio / coordenadas | El dato y su botón no se muestran |
| Cervecería sin ícono, o ícono 404 | `placeholder` y `error` de Picasso: el vaso dibujado |
| El celular no tiene marcador, navegador o mapas | `try/catch` de `ActivityNotFoundException` y Toast |
| Se rota la pantalla | El ViewModel sobrevive: no se vuelve a llamar a la API |
| Modo avión | Mis cervecerías y el detalle de una guardada siguen andando |

Todos los errores quedan además en el Logcat con `Log.e` y un `TAG` por clase
(`BreweryRepository`, `FavoriteRepository`).

Hay dos controles de red a propósito: `NetworkChecker` evita salir a la API cuando
ya se sabe que no hay conexión (el usuario ve el aviso al toque, sin esperar el
timeout), y la `IOException` cubre el caso de que la red se caiga en el medio de la
llamada.

## Patrones de diseño

| Patrón | Dónde está |
|---|---|
| **MVVM** | `viewmodel/` + los Fragments y Activities que observan |
| **Repository** | `repository/BreweryRepository.kt` y `data/FavoriteRepository.kt` |
| **Singleton** | `network/RetrofitClient.kt`, que es un `object` |
| **Factory** | `BreweryViewModelFactory`, `FavoritesViewModelFactory`, `BreweryDetailViewModelFactory` |
| **Adapter** | `adapter/BreweryAdapter.kt` y `adapter/FavoriteAdapter.kt` |
| **Observer** | `LiveData.observe()` en los Fragments y `collectAsState()` en Compose |

Además, ni los ViewModels ni los repositorios crean sus propias dependencias: las
reciben por constructor (**inyección de dependencias**). El Fragment es el que arma
la cadena:

```kotlin
private val viewModel: BreweryViewModel by viewModels {
    BreweryViewModelFactory(
        repository = BreweryRepository(
            apiService = RetrofitClient.breweryApiService,
            networkChecker = NetworkChecker(requireContext().applicationContext)
        )
    )
}
```

La fábrica hace falta porque `BreweryViewModel` no tiene constructor vacío, así que
Android no sabe cómo construirlo solo. La ventaja de hacerlo así es que para testear
se le podría pasar un repositorio falso sin tocar el ViewModel.

## Base de datos

Una sola tabla, `breweries`, creada en `DatabaseHelper`:

| Columna | Tipo | |
|---|---|---|
| `id` | TEXT | PRIMARY KEY, el mismo id que manda la API |
| `name` | TEXT | NOT NULL |
| `brewery_type` | TEXT | |
| `address` | TEXT | la dirección ya armada |
| `city` | TEXT | |
| `country` | TEXT | |
| `phone` | TEXT | |
| `website_url` | TEXT | |
| `latitude` | REAL | |
| `longitude` | REAL | |
| `status` | TEXT | NOT NULL, `visited` o `pending` |
| `saved_at` | TEXT | NOT NULL, fecha y hora en formato año-mes-día hora |

Que el `id` de la API sea la clave primaria evita que la misma cervecería se guarde
dos veces. La fecha se guarda como `2026-11-04 18:30:00` porque así ordena bien
alfabéticamente, que es como la compara SQLite en el `ORDER BY`, incluso entre las
guardadas el mismo día. Al mostrarla se da vuelta y se le saca la hora.

Las cuatro operaciones están en `FavoriteRepository`: `insertBrewery` (con
`ContentValues`), `getBreweriesByStatus` y `getBreweryById` (con `db.query` y un
`Cursor`), `updateStatus` y `deleteBrewery`. Todas las condiciones usan `?` y
`arrayOf(...)` en vez de pegar el valor dentro del string, que es lo que protege de
la inyección SQL.

## Tecnologías

- Kotlin
- **Retrofit 3.0.0** + **Gson** para consumir la API
- **Picasso 2.8** para descargar y mostrar los íconos
- **SQLite** con `SQLiteOpenHelper` (sin Room, a mano)
- **ViewModel + LiveData** (androidx.lifecycle) en las pantallas de XML
- **Jetpack Compose** (BOM 2026.06.00) + **Material 3** en la pantalla de detalle
- **RecyclerView** con Adapter y ViewHolder propios
- **Fragments** con `supportFragmentManager` y back stack
- **View Binding** (sin `findViewById` en ningún lado)
- **Material 3** para los componentes y el tema, con modo oscuro

## Cómo correr el proyecto

1. Abrir la carpeta con Android Studio y esperar a que termine el Gradle Sync.
2. Elegir un emulador o un dispositivo con Android 7.0 (API 24) o superior.
3. Darle a Run.

Hace falta conexión a internet, tanto para el sync de Gradle como para que la app
pueda consultar la API. Mis cervecerías y el detalle de una cervecería ya guardada
funcionan sin conexión.

- `minSdk` 24
- `targetSdk` / `compileSdk` 36
