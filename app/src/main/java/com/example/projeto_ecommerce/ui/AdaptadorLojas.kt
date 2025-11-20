package com.example.projeto_ecommerce.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projeto_ecommerce.R
import com.example.projeto_ecommerce.model.Loja

class AdaptadorLojas(private val onClick: (Loja) -> Unit) :
    ListAdapter<Loja, AdaptadorLojas.LojaVH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Loja>() {
            override fun areItemsTheSame(oldItem: Loja, newItem: Loja): Boolean =
                (oldItem.storeId ?: oldItem.id) == (newItem.storeId ?: newItem.id)

            override fun areContentsTheSame(oldItem: Loja, newItem: Loja): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LojaVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_loja, parent, false)
        return LojaVH(v)
    }

    override fun onBindViewHolder(holder: LojaVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LojaVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvNome: TextView = itemView.findViewById(R.id.tvStoreName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvStoreDesc)
        private val ivThumb: ImageView = itemView.findViewById(R.id.ivStoreThumb)

        fun bind(loja: Loja) {

            tvNome.text = loja.nome ?: "Loja"
            tvDesc.text = loja.descricao ?: ""

            // Se tiver URL da imagem, usa setImageURI()
            if (!loja.imagem.isNullOrEmpty()) {
                try {
                    ivThumb.setImageURI(Uri.parse(loja.imagem))
                } catch (e: Exception) {
                    ivThumb.setImageResource(R.drawable.ic_delete)
                }
            } else {
                // fallback para drawable
                ivThumb.setImageResource(R.drawable.ic_delete)
            }

            itemView.setOnClickListener {
                onClick(loja)
            }
        }
    }
}
