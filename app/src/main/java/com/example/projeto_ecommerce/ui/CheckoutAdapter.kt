package com.example.projeto_ecommerce.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projeto_ecommerce.R

class CheckoutAdapter(
    private val items: MutableList<CartItem>,
    private val onRemove: (produtoId: String) -> Unit,
    private val onQtyChanged: (produtoId: String, novaQty: Int) -> Unit
) : RecyclerView.Adapter<CheckoutAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvPreco: TextView = view.findViewById(R.id.tvPreco)
        val tvQuantidade: TextView = view.findViewById(R.id.tvQuantidade)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnRemove: Button = view.findViewById(R.id.btnRemover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_checkout, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvTitulo.text = item.titulo
        holder.tvPreco.text = "R$ %.2f".format(item.preco)
        holder.tvQuantidade.text = item.quantidade.toString()

        holder.btnRemove.setOnClickListener {
            onRemove(item.produtoId)
        }

        holder.btnMinus.setOnClickListener {
            if (item.quantidade > 1) {
                item.quantidade -= 1
                holder.tvQuantidade.text = item.quantidade.toString()
                onQtyChanged(item.produtoId, item.quantidade)
            }
        }

        holder.btnPlus.setOnClickListener {
            item.quantidade += 1
            holder.tvQuantidade.text = item.quantidade.toString()
            onQtyChanged(item.produtoId, item.quantidade)
        }
    }

    override fun getItemCount(): Int = items.size

    fun update(list: List<CartItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}
