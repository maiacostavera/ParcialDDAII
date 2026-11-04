package com.example.parcial2breweryguide.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parcial2breweryguide.R
import com.example.parcial2breweryguide.databinding.ItemBreweryBinding
import com.example.parcial2breweryguide.model.Brewery
import com.squareup.picasso.Picasso

/**
 * Conecta la lista de cervecerias con el RecyclerView: agarra cada Brewery y la
 * pega en una fila de item_brewery.xml (patron Adapter).
 *
 * @param breweryList lista a mostrar. Arranca vacia y se llena cuando el ViewModel responde.
 * @param onItemClick que hacer cuando tocan una fila. Lo decide el Fragment, el
 *                    adapter solo avisa.
 */
class BreweryAdapter(
    private var breweryList: List<Brewery>,
    private val onItemClick: (Brewery) -> Unit
) : RecyclerView.Adapter<BreweryAdapter.BreweryViewHolder>() {

    /**
     * Sostiene las vistas de UNA fila. Guardamos el binding de item_brewery.xml en
     * vez de cada vista por separado, asi el RecyclerView recicla la fila sin
     * volver a buscarlas. binding.root es la MaterialCardView de afuera.
     */
    class BreweryViewHolder(val binding: ItemBreweryBinding) :
        RecyclerView.ViewHolder(binding.root)

    /** Infla item_brewery.xml y lo mete en un ViewHolder. Se llama pocas veces. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreweryViewHolder {
        val binding = ItemBreweryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BreweryViewHolder(binding)
    }

    override fun getItemCount() = breweryList.size

    /** Llena la fila de esa posicion con los datos. Se llama muchas veces. */
    override fun onBindViewHolder(holder: BreweryViewHolder, position: Int) {
        val brewery = breweryList[position]
        val context = holder.binding.root.context

        holder.binding.apply {
            tvBreweryName.text = brewery.name
            tvBreweryType.text = context.getString(brewery.getTypeLabelRes())

            // En las muestras siempre vino, pero la API no lo garantiza.
            tvBreweryCity.text = brewery.city ?: context.getString(R.string.not_available)

            // El icono sale del sitio web de la cerveceria, y muchas no tienen.
            val iconUrl = brewery.getIconUrl()
            if (iconUrl != null) {
                Picasso.get()
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_brewery_placeholder)
                    .error(R.drawable.ic_brewery_placeholder)
                    .into(ivBreweryIcon)
            } else {
                // Si no hay URL cortamos cualquier descarga anterior de esta fila
                // reciclada y dejamos el dibujo del vaso.
                Picasso.get().cancelRequest(ivBreweryIcon)
                ivBreweryIcon.setImageResource(R.drawable.ic_brewery_placeholder)
            }

            root.setOnClickListener { onItemClick(brewery) }
        }
    }

    /** Reemplaza la lista y redibuja, cuando el ViewModel publica resultados nuevos. */
    fun updateList(newList: List<Brewery>) {
        breweryList = newList
        notifyDataSetChanged()
    }
}
