package com.example.projeto_ecommerce.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projeto_ecommerce.databinding.ActivityVendedorBinding
import com.example.projeto_ecommerce.databinding.DialogProductBinding
import com.example.projeto_ecommerce.model.Produto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class VendedorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVendedorBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdaptadorDeProdutosVendedor
    private val list = mutableListOf<Produto>()
    private val TAG = "VendedorActivity"

    private var storeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = Firebase.firestore

        // Obtém o storeId salvo no login
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        storeId = prefs.getString("storeId", null)

        if (storeId == null) {
            Toast.makeText(this, "Nenhuma loja encontrada. Crie sua loja antes.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        adapter = AdaptadorDeProdutosVendedor(
            items = list,
            onEdit = { produto -> showProductDialog(produto, true) },
            onDelete = { produto -> confirmDelete(produto) }
        )

        binding.recyclerProdutos.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerProdutos.adapter = adapter

        binding.btnAddTop.setOnClickListener { showProductDialog(null, false) }

        loadProducts()
    }

    private fun loadProducts() {
        Log.d(TAG, "Carregando produtos da loja: $storeId")

        db.collection("Produtos")
            .whereEqualTo("storeId", storeId)
            .get()
            .addOnSuccessListener { snap ->
                val data = mutableListOf<Produto>()
                for (doc in snap.documents) {
                    val p = doc.toObject(Produto::class.java)
                    if (p != null) {
                        p.id = doc.id
                        data.add(p)
                    }
                }
                adapter.update(data)
                Toast.makeText(this, "Produtos carregados: ${data.size}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Erro ao carregar produtos", e)
            }
    }

    private fun showProductDialog(product: Produto?, isEdit: Boolean) {
        val inflater = LayoutInflater.from(this)
        val dialogB = DialogProductBinding.inflate(inflater)
        val dialog = AlertDialog.Builder(this).setView(dialogB.root).create()

        val tiposDrawable = resources.getStringArray(com.example.projeto_ecommerce.R.array.lista_imagens_produto)
        val adapterDrop = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposDrawable)
        dialogB.autoCompleteImage.setAdapter(adapterDrop)

        if (isEdit && product != null) {
            dialogB.inputTitulo.setText(product.titulo)
            dialogB.inputDescricao.setText(product.descricao)
            dialogB.inputPreco.setText(product.preco.toString())
            dialogB.autoCompleteImage.setText(product.imagem, false)
        }

        dialogB.btnSave.setOnClickListener {
            val titulo = dialogB.inputTitulo.text.toString().trim()
            val descricao = dialogB.inputDescricao.text.toString().trim()
            val preco = dialogB.inputPreco.text.toString().toDoubleOrNull() ?: 0.0
            val imagem = dialogB.autoCompleteImage.text.toString().trim()

            if (titulo.isEmpty()) {
                dialogB.inputTituloLayout.error = "Obrigatório"
                return@setOnClickListener
            }
            dialogB.inputTituloLayout.error = null
            dialogB.btnSave.isEnabled = false

            val currentStoreId = storeId

            if (isEdit && product != null) {
                val updated = product.copy(
                    titulo = titulo,
                    descricao = descricao,
                    preco = preco,
                    imagem = imagem,
                    storeId = currentStoreId
                )

                db.collection("Produtos").document(product.id).set(updated)
                    .addOnSuccessListener {
                        adapter.replace(updated)
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        dialogB.btnSave.isEnabled = true
                        Toast.makeText(this, "Erro ao atualizar: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            } else {
                if (currentStoreId == null) {
                    Toast.makeText(this, "Erro: loja não encontrada.", Toast.LENGTH_LONG).show()
                    dialogB.btnSave.isEnabled = true
                    return@setOnClickListener
                }

                val novo = Produto(
                    titulo = titulo,
                    descricao = descricao,
                    preco = preco,
                    imagem = imagem,
                    storeId = currentStoreId
                )

                db.collection("Produtos").add(novo)
                    .addOnSuccessListener { ref ->
                        novo.id = ref.id
                        adapter.add(novo)
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        dialogB.btnSave.isEnabled = true
                        Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        dialogB.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun confirmDelete(product: Produto) {
        val dlg = AlertDialog.Builder(this)
            .setTitle("Remover produto")
            .setMessage("Deseja remover '${product.titulo}'?")
            .setPositiveButton("Remover") { _, _ ->
                db.collection("Produtos").document(product.id).delete()
                    .addOnSuccessListener {
                        adapter.removeById(product.id)
                        Toast.makeText(this, "Removido", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao remover: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dlg.show()
    }
}
