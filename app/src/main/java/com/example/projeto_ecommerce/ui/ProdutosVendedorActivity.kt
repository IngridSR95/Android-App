package com.example.projeto_ecommerce.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projeto_ecommerce.databinding.ActivityVendedorBinding
import com.example.projeto_ecommerce.model.Produto

class ProdutosVendedorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVendedorBinding
    private lateinit var adapter: AdaptadorDeProdutosVendedor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val produtos = mutableListOf(
            Produto(id = "1", titulo = "Cookie Tradicional", descricao = "Cookie com gotas de chocolate", preco = 12.00, imagem = "tradicional"),
            Produto(id = "2", titulo = "Red Velvet", descricao = "Cookie com massa red velvet e recheio", preco = 15.00, imagem = "redvelvet"),
            Produto(id = "3", titulo = "Brownie", descricao = "Brownie tradicional", preco = 10.00, imagem = "brownie"),
            Produto(id = "4", titulo = "Pedaço de Brownie", descricao = "Brownie individual recheado", preco = 8.00, imagem = "pedaco_brownie"),
            Produto(id = "5", titulo = "Recheado", descricao = "Cookie recheado especial", preco = 14.00, imagem = "recheado"),
            Produto(id = "6", titulo = "Tradicional", descricao = "Cookie clássico artesanal", preco = 11.00, imagem = "tradicional")
        )

        adapter = AdaptadorDeProdutosVendedor(
            items = produtos,
            onEdit = { produto ->

            },
            onDelete = { produto ->
                // Aqui apagaria do Firestore, mas agora só removemos da lista
                val index = produtos.indexOf(produto)
                if (index >= 0) {
                    produtos.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
            }
        )

        binding.recyclerProdutos.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerProdutos.adapter = adapter
    }
}
