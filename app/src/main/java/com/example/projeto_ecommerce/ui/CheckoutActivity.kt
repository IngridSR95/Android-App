package com.example.projeto_ecommerce.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projeto_ecommerce.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var adapter: AdaptadorCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // sempre carregar do prefs antes de mostrar
        CartManager.load(this)

        // cria adapter com callback que REMOVE no CartManager e chama refresh()
        adapter = AdaptadorCart(CartManager.all().toMutableList(), onRemove = { produtoId ->
            CartManager.remove(this, produtoId)   // remove e salva internamente
            refresh()
        })

        binding.recyclerCart.layoutManager = LinearLayoutManager(this)
        binding.recyclerCart.adapter = adapter

        refresh()

        binding.btnComprar.setOnClickListener {
            Toast.makeText(this, "Compra realizada! Total: R$ %.2f".format(CartManager.total()), Toast.LENGTH_LONG).show()
            CartManager.clear(this)
            refresh()
            finish()
        }
    }

    private fun refresh() {
        // garante que o adapter sempre receba a fonte de verdade
        CartManager.load(this) // recarrega do SharedPreferences (opcional, já foram salvas por remove)
        val list = CartManager.all().toMutableList()
        adapter.update(list)
        binding.tvTotal.text = "Total: R$ %.2f".format(CartManager.total())

        // mostra texto de vazio se necessário
        binding.tvEmptyCheckout.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}
