package com.example.projeto_ecommerce.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projeto_ecommerce.R
import com.example.projeto_ecommerce.databinding.ActivityProdutosBinding
import com.example.projeto_ecommerce.model.Produto
import com.google.firebase.firestore.FirebaseFirestore

class ProdutosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProdutosBinding
    private lateinit var adapter: AdaptadorDeProdutos
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val TAG = "ProdutosActivity"

    private var storeId: String? = null
    private var storeName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdutosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // lê extras
        storeId = intent.getStringExtra("storeId")
        storeName = intent.getStringExtra("storeName")

        // título
        binding.tvTituloProdutos.text = storeName ?: "Produtos"

        // botão voltar (opcional)
        binding.root.findViewById<ImageView?>(R.id.btnVoltar)?.setOnClickListener { finish() }

        // botão carrinho (opcional no layout: id = btnCart)
        binding.root.findViewById<ImageView?>(R.id.btnCart)?.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        // adapter com callback de clique
        val listaInicial = mutableListOf<Produto>()
        adapter = AdaptadorDeProdutos(listaInicial) { produto ->
            // ao clicar em produto -> abrir diálogo de quantidade/adicionar
            showProdutoDialog(produto)
        }

        binding.recyclerProdutos.layoutManager = LinearLayoutManager(this)
        binding.recyclerProdutos.adapter = adapter

        if (storeId.isNullOrEmpty()) {
            // força voltar pra seleção de lojas pra evitar confusão
            startActivity(Intent(this, LojasActivity::class.java))
            finish()
            return
        }

        carregarProdutosDaLoja(storeId!!)
    }

    private fun carregarProdutosDaLoja(storeId: String) {
        Log.d(TAG, "carregarProdutosDaLoja: storeId=$storeId")
        binding.progress.visibility = android.view.View.VISIBLE
        binding.tvVazio.visibility = android.view.View.GONE

        // use o nome exato da collection no seu Firestore (aqui: "Produtos")
        db.collection("Produtos")
            .whereEqualTo("storeId", storeId)
            .get()
            .addOnSuccessListener { snap ->
                Log.d(TAG, "produtos snapshot size=${snap.size()}")
                val lista = mutableListOf<Produto>()
                for (doc in snap.documents) {
                    val p = doc.toObject(Produto::class.java)
                    if (p != null) {
                        p.id = doc.id
                        lista.add(p)
                    } else {
                        Log.w(TAG, "Documento produto id=${doc.id} não mapeou para Produto")
                    }
                }

                adapter.update(lista)
                binding.progress.visibility = android.view.View.GONE

                if (lista.isEmpty()) {
                    binding.tvVazio.visibility = android.view.View.VISIBLE
                    binding.tvVazio.text = "Nenhum produto encontrado nesta loja."
                } else {
                    binding.tvVazio.visibility = android.view.View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao carregar produtos da loja", e)
                binding.progress.visibility = android.view.View.GONE
                binding.tvVazio.visibility = android.view.View.VISIBLE
                binding.tvVazio.text = "Erro ao carregar produtos."
                Toast.makeText(this, "Erro ao carregar produtos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showProdutoDialog(prod: Produto) {
        val dlgView = LayoutInflater.from(this).inflate(R.layout.dialog_produto_quantidade, null)
        val iv = dlgView.findViewById<ImageView>(R.id.ivDialogImagem)
        val tvTitulo = dlgView.findViewById<TextView>(R.id.tvDialogTitulo)
        val tvPreco = dlgView.findViewById<TextView>(R.id.tvDialogPreco)
        val etQuantidade = dlgView.findViewById<EditText>(R.id.etQuantidade)
        val btnAdd = dlgView.findViewById<TextView>(R.id.btnAddCarrinho)

        tvTitulo.text = prod.titulo
        tvPreco.text = "R$ %.2f".format(prod.preco)
        etQuantidade.setText("1")

        // imagem local fallback
        val ctx = iv.context
        val imgName = if (prod.imagem.isNullOrEmpty()) "cookie_tradicional" else prod.imagem
        val imgId = ctx.resources.getIdentifier(imgName, "drawable", ctx.packageName)
        if (imgId != 0) iv.setImageResource(imgId)

        val dialog = AlertDialog.Builder(this).setView(dlgView).create()

        btnAdd.setOnClickListener {
            val q = etQuantidade.text.toString().toIntOrNull() ?: 1
            val item = CartItem(
                produtoId = prod.id,
                titulo = prod.titulo ?: "",
                preco = prod.preco,
                quantidade = q,
                imagem = prod.imagem,
                storeId = prod.storeId
            )
            CartManager.add(this, item)
            Toast.makeText(this, "Adicionado ao carrinho", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
