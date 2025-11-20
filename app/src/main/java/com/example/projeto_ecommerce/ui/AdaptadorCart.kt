package com.example.projeto_ecommerce.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projeto_ecommerce.databinding.ItemCartBinding

class AdaptadorCart(
    private var items: MutableList<CartItem>,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<AdaptadorCart.VH>() {

    inner class VH(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvTituloCart.text = item.titulo
        holder.b.tvQuantidadeCart.text = "x${item.quantidade}"
        holder.b.tvPrecoCart.text = "R$ %.2f".format(item.preco * item.quantidade)

        // usa bindingAdapterPosition para garantir que a posição está atualizada
        holder.b.btnRemoverCart.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val produtoId = items[pos].produtoId
                onRemove(produtoId)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun update(nova: List<CartItem>) {
        items.clear()
        items.addAll(nova)
        notifyDataSetChanged()
    }
}
