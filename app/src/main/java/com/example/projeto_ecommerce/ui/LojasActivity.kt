package com.example.projeto_ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projeto_ecommerce.databinding.ActivityLojasBinding
import com.example.projeto_ecommerce.model.Loja
import com.google.firebase.firestore.FirebaseFirestore

class LojasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLojasBinding
    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: AdaptadorLojas
    private val TAG = "LojasActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLojasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdaptadorLojas { loja ->
            // Ao clicar em uma loja, abrir a tela de produtos passando o storeId
            val i = Intent(this, ProdutosActivity::class.java)
            i.putExtra("storeId", loja.storeId ?: loja.id)
            i.putExtra("storeName", loja.nome)
            startActivity(i)
        }

        binding.recyclerLojas.layoutManager = LinearLayoutManager(this)
        binding.recyclerLojas.adapter = adapter

        carregarLojas()
    }

    private fun carregarLojas() {
        binding.progress.visibility = View.VISIBLE

        // Query para pegar apenas documentos da coleção "lojas"
        db.collection("lojas")
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { snap ->
                val lista = mutableListOf<Loja>()
                for (doc in snap.documents) {
                    val s = doc.toObject(Loja::class.java)
                    if (s != null) {
                        // garante que cada loja tem ids consistentes
                        if (s.storeId.isNullOrEmpty()) s.storeId = doc.id
                        if (s.id.isNullOrEmpty()) s.id = doc.id
                        lista.add(s)
                    }
                }

                // envia a lista de lojas pro adapter (apenas nomes/descrição/imagem)
                adapter.submitList(lista)
                binding.progress.visibility = View.GONE
                binding.tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao carregar lojas", e)
                binding.progress.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                Toast.makeText(this, "Erro ao carregar lojas: ${e.message}", Toast.LENGTH_LONG).show()
            }
        binding.btnVoltar.setOnClickListener {
            finish()
        }

    }
}
