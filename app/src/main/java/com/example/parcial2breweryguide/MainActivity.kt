package com.example.parcial2breweryguide

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.parcial2breweryguide.databinding.ActivityMainBinding

/**
 * Pantalla principal. No dibuja contenido propio: tiene la barra de arriba y un
 * contenedor donde se van cambiando los Fragments.
 *
 * Arranca mostrando BreweryListFragment y, desde el icono de la barra, pone
 * FavoritesFragment encima. Como esa segunda transaccion va al back stack, el
 * boton atras del celular vuelve solo al listado.
 */
class MainActivity : AppCompatActivity() {

    // View Binding: acceso a las vistas de activity_main.xml sin findViewById.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        setupToolbar()

        // Solo montamos el listado la primera vez. Si la pantalla se recrea (al
        // rotar), el FragmentManager ya tiene sus fragments guardados y los vuelve
        // a poner solo: si no chequearamos esto, quedarian dos encimados.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BreweryListFragment())
                .commit()
        }

        // Al rotar, el back stack se restaura pero el listener de abajo no se
        // dispara, asi que acomodamos la barra una vez a mano.
        updateToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_favorites) {
                showFavorites()
                true
            } else {
                false
            }
        }

        // El back stack solo tiene algo cuando estamos en "Mis cervecerias", asi
        // que alcanza con mirarlo para saber que titulo poner.
        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbar()
        }
    }

    /** Pone el fragment de favoritas encima del listado. */
    private fun showFavorites() {
        // Si ya estamos en favoritas no hacemos nada, para no apilar dos iguales.
        if (supportFragmentManager.backStackEntryCount > 0) return

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FavoritesFragment())
            // addToBackStack guarda esta transaccion, asi el boton atras la deshace
            // y vuelve al listado en vez de cerrar la app.
            .addToBackStack(null)
            .commit()
    }

    /** Cambia el titulo y esconde el boton de favoritas cuando ya estamos ahi. */
    private fun updateToolbar() {
        val onList = supportFragmentManager.backStackEntryCount == 0

        binding.toolbar.title = getString(
            if (onList) R.string.app_name else R.string.title_favorites
        )
        binding.toolbar.menu.findItem(R.id.action_favorites)?.isVisible = onList
    }

    /**
     * Como la app va de punta a punta de la pantalla (edge to edge), agregamos
     * padding para que el contenido no quede debajo de las barras del sistema.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
