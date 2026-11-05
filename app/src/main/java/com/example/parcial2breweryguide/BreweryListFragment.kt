package com.example.parcial2breweryguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parcial2breweryguide.adapter.BreweryAdapter
import com.example.parcial2breweryguide.databinding.FragmentBreweryListBinding
import com.example.parcial2breweryguide.network.NetworkChecker
import com.example.parcial2breweryguide.network.RetrofitClient
import com.example.parcial2breweryguide.repository.BreweryRepository
import com.example.parcial2breweryguide.viewmodel.BreweryViewModel
import com.example.parcial2breweryguide.viewmodel.BreweryViewModelFactory

/**
 * Fragment principal que pide la consigna: buscador arriba y listado de
 * cervecerias en un RecyclerView.
 *
 * Es la "V" de MVVM: solo muestra y escucha toques. Las llamadas a la API las
 * hace el ViewModel; este archivo se limita a observar los LiveData.
 */
class BreweryListFragment : Fragment() {

    // El binding se crea en onCreateView y se tira en onDestroyView, porque la
    // vista del fragment puede morir antes que el fragment. Por eso es nullable.
    private var _binding: FragmentBreweryListBinding? = null

    // Atajo para no escribir _binding!! en todos lados. Solo valido mientras la
    // vista existe (entre onCreateView y onDestroyView).
    private val binding get() = _binding!!

    private lateinit var adapter: BreweryAdapter

    // El delegate crea el ViewModel, o recupera el que ya existia si la pantalla
    // se volvio a crear (por ejemplo al rotar).
    //
    // Aca es donde se arman las dependencias y se inyectan: el Fragment construye
    // el repositorio con lo que necesita y se lo pasa al ViewModel a traves de la
    // fabrica. El ViewModel no se crea nada por su cuenta, solo usa lo que recibe.
    private val viewModel: BreweryViewModel by viewModels {
        BreweryViewModelFactory(
            repository = BreweryRepository(
                apiService = RetrofitClient.breweryApiService,
                // applicationContext y no el del fragment, asi el NetworkChecker
                // no queda agarrado a una vista que se puede destruir.
                networkChecker = NetworkChecker(requireContext().applicationContext)
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBreweryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    /** Deja la lista lista para usar: orientacion vertical y adapter enganchado. */
    private fun setupRecyclerView() {
        adapter = BreweryAdapter(emptyList()) { brewery ->
            // Tocaron una cerveceria: abrimos el detalle mandandole solo el id.
            // Con el id alcanza porque la pantalla de detalle va a buscar los datos
            // a la base o a la API.
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_BREWERY_ID, brewery.id)
            startActivity(intent)
        }

        binding.rvBreweries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBreweries.adapter = adapter
    }

    /** Valida lo que escribio el usuario y se lo pasa al ViewModel. */
    private fun setupClickListeners() {
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()

            if (query.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_empty_query, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            hideKeyboard()
            scrollListToTop()

            // El radio button elegido decide si se usa by_city o by_name.
            val filterIsCity = binding.rbCity.isChecked
            viewModel.searchBreweries(query, filterIsCity)
        }

        binding.btnShowAll.setOnClickListener {
            binding.etSearch.text?.clear()
            hideKeyboard()
            scrollListToTop()
            viewModel.loadAllBreweries()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
    }

    /** Cierra el teclado, porque si no tapa los resultados de la busqueda. */
    private fun hideKeyboard() {
        val manager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        binding.etSearch.clearFocus()
    }

    /**
     * El RecyclerView conserva el scroll aunque cambie la lista, asi que sin esto
     * los resultados de una busqueda nueva aparecerian desde la mitad.
     */
    private fun scrollListToTop() {
        binding.rvBreweries.scrollToPosition(0)
    }

    /**
     * Nos suscribimos a los LiveData del ViewModel. Cada vez que cambian, estos
     * bloques corren solos y refrescan la pantalla.
     *
     * viewLifecycleOwner y no "this": el observer tiene que morir con la vista del
     * fragment, no con el fragment, o quedaria apuntando a vistas que ya no existen.
     */
    private fun observeViewModel() {
        viewModel.breweries.observe(viewLifecycleOwner) { breweries ->
            adapter.updateList(breweries)
            binding.rvBreweries.visibility =
                if (breweries.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Mientras carga bloqueamos los botones para no disparar dos busquedas.
            binding.btnSearch.isEnabled = !isLoading
            binding.btnShowAll.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                binding.tvMessage.text = message
                binding.tvMessage.visibility = View.VISIBLE
            } else {
                binding.tvMessage.visibility = View.GONE
            }
        }

        viewModel.canRetry.observe(viewLifecycleOwner) { canRetry ->
            binding.btnRetry.visibility = if (canRetry) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para no dejar la vista colgada en memoria.
        _binding = null
    }
}
