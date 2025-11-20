package com.example.projeto_ecommerce.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projeto_ecommerce.databinding.ItemProdutoBinding
import com.example.projeto_ecommerce.model.Produto

class AdaptadorDeProdutos(
    val items: MutableList<Produto>,
    private val onClick: (Produto) -> Unit
) : RecyclerView.Adapter<AdaptadorDeProdutos.VH>() {

    inner class VH(val b: ItemProdutoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemProdutoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        val ctx = holder.itemView.context
        val imgName = if (!p.imagem.isNullOrEmpty()) p.imagem else "cookie_tradicional"
        val imgId = ctx.resources.getIdentifier(imgName, "drawable", ctx.packageName)
        if (imgId != 0) holder.b.imgProduto.setImageResource(imgId) else holder.b.imgProduto.setImageResource(android.R.color.transparent)
        holder.b.tvTitulo.text = p.titulo
        holder.b.tvDescricao.text = p.descricao
        holder.b.tvPreco.text = "R$ %.2f".format(p.preco)

        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount(): Int = items.size

    fun update(list: List<Produto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun add(product: Produto) {
        items.add(0, product)
        notifyItemInserted(0)
    }

    fun removeById(id: String) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    fun replace(product: Produto) {
        val idx = items.indexOfFirst { it.id == product.id }
        if (idx >= 0) {
            items[idx] = product
            notifyItemChanged(idx)
        }
    }
}
