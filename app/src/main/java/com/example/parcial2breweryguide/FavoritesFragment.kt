package com.example.parcial2breweryguide

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parcial2breweryguide.adapter.FavoriteAdapter
import com.example.parcial2breweryguide.data.FavoriteRepository
import com.example.parcial2breweryguide.databinding.FragmentFavoritesBinding
import com.example.parcial2breweryguide.model.BreweryDetails
import com.example.parcial2breweryguide.viewmodel.FavoritesViewModel
import com.example.parcial2breweryguide.viewmodel.FavoritesViewModelFactory
import com.google.android.material.tabs.TabLayout

/**
 * "Mis cervecerias": las que el usuario guardo en SQLite, separadas en dos
 * pestañas (Visitadas y Pendientes).
 *
 * No toca la API, asi que funciona igual sin internet.
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FavoriteAdapter

    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(
            repository = FavoriteRepository(requireContext().applicationContext)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        observeViewModel()
    }

    /**
     * Releemos la base cada vez que la pantalla vuelve al frente, asi se ve lo que
     * se haya guardado o cambiado desde el detalle.
     */
    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter(
            breweryList = emptyList(),
            onItemClick = { brewery ->
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_BREWERY_ID, brewery.id)
                startActivity(intent)
            },
            onToggleClick = { brewery -> viewModel.toggleStatus(brewery) },
            onDeleteClick = { brewery -> confirmDelete(brewery) }
        )

        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter
    }

    private fun setupTabs() {
        // Dejamos marcada la pestaña que ya tenia el ViewModel. Hace falta al
        // rotar, porque la vista se crea de nuevo pero el ViewModel sobrevive.
        val tabIndex =
            if (viewModel.selectedStatus == BreweryDetails.STATUS_VISITED) 0 else 1
        binding.tabLayout.getTabAt(tabIndex)?.select()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                val status = if (tab.position == 0) {
                    BreweryDetails.STATUS_VISITED
                } else {
                    BreweryDetails.STATUS_PENDING
                }
                viewModel.selectStatus(status)
            }

            // Los otros dos no nos sirven, pero la interfaz obliga a declararlos.
            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun observeViewModel() {
        viewModel.favorites.observe(viewLifecycleOwner) { breweries ->
            adapter.updateList(breweries)

            // Cuando no hay nada guardado mostramos el cartel que explica como hacerlo.
            val isEmpty = breweries.isEmpty()
            binding.rvFavorites.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    /** Antes de borrar pedimos confirmacion, porque no se puede deshacer. */
    private fun confirmDelete(brewery: BreweryDetails) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, brewery.name))
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                viewModel.deleteBrewery(brewery)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
