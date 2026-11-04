package com.example.parcial2breweryguide.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parcial2breweryguide.R
import com.example.parcial2breweryguide.databinding.ItemFavoriteBinding
import com.example.parcial2breweryguide.model.BreweryDetails
import com.squareup.picasso.Picasso

/**
 * Adapter de la lista de "Mis cervecerias".
 *
 * Es parecido a BreweryAdapter pero muestra BreweryDetails (lo que sale de
 * SQLite) y cada fila tiene dos botones mas: cambiar el estado y borrar.
 *
 * @param onItemClick abrir el detalle.
 * @param onToggleClick pasar de visitada a pendiente y al reves.
 * @param onDeleteClick borrar. El Fragment es el que pide la confirmacion.
 */
class FavoriteAdapter(
    private var breweryList: List<BreweryDetails>,
    private val onItemClick: (BreweryDetails) -> Unit,
    private val onToggleClick: (BreweryDetails) -> Unit,
    private val onDeleteClick: (BreweryDetails) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(val binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun getItemCount() = breweryList.size

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val brewery = breweryList[position]
        val context = holder.binding.root.context

        holder.binding.apply {
            tvFavoriteName.text = brewery.name
            tvFavoriteType.text = context.getString(brewery.getTypeLabelRes())

            // Ciudad y pais juntos, salteando el que falte.
            tvFavoriteCity.text = listOfNotNull(brewery.city, brewery.country)
                .joinToString(", ")
                .ifEmpty { context.getString(R.string.not_available) }

            tvFavoriteSavedAt.text =
                context.getString(R.string.saved_at_format, formatDate(brewery.savedAt))

            // El boton ofrece siempre el estado contrario al que tiene ahora.
            btnToggleStatus.text = if (brewery.status == BreweryDetails.STATUS_VISITED) {
                context.getString(R.string.button_mark_pending)
            } else {
                context.getString(R.string.button_mark_visited)
            }

            val iconUrl = brewery.getIconUrl()
            if (iconUrl != null) {
                Picasso.get()
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_brewery_placeholder)
                    .error(R.drawable.ic_brewery_placeholder)
                    .into(ivFavoriteIcon)
            } else {
                Picasso.get().cancelRequest(ivFavoriteIcon)
                ivFavoriteIcon.setImageResource(R.drawable.ic_brewery_placeholder)
            }

            root.setOnClickListener { onItemClick(brewery) }
            btnToggleStatus.setOnClickListener { onToggleClick(brewery) }
            btnDelete.setOnClickListener { onDeleteClick(brewery) }
        }
    }

    fun updateList(newList: List<BreweryDetails>) {
        breweryList = newList
        notifyDataSetChanged()
    }

    /**
     * La fecha se guarda como ano-mes-dia y hora para que SQLite la ordene bien, pero
     * se muestra sin la hora y al reves, que es como la escribimos en castellano.
     */
    private fun formatDate(savedAt: String?): String {
        if (savedAt == null) return ""

        val parts = savedAt.substringBefore(" ").split("-")
        if (parts.size != 3) return savedAt

        return "${parts[2]}/${parts[1]}/${parts[0]}"
    }
}
