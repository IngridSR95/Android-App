package com.example.projeto_ecommerce.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class CartItem(
    val produtoId: String,
    val titulo: String,
    val preco: Double,
    var quantidade: Int,
    val imagem: String?,
    val storeId: String?
)

object CartManager {
    private const val PREF = "app_prefs"
    private const val KEY = "cart_json"
    private val items = mutableListOf<CartItem>()

    fun load(context: Context) {
        items.clear()
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, null) ?: return
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                items.add(
                    CartItem(
                        produtoId = o.getString("produtoId"),
                        titulo = o.getString("titulo"),
                        preco = o.getDouble("preco"),
                        quantidade = o.getInt("quantidade"),
                        imagem = o.optString("imagem", null),
                        storeId = o.optString("storeId", null)
                    )
                )
            }
        } catch (ex: Exception) {
            // opcional: logar ex.message
        }
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray()
        for (it in items) {
            val o = JSONObject()
            o.put("produtoId", it.produtoId)
            o.put("titulo", it.titulo)
            o.put("preco", it.preco)
            o.put("quantidade", it.quantidade)
            o.put("imagem", it.imagem)
            o.put("storeId", it.storeId)
            arr.put(o)
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun add(context: Context, item: CartItem) {
        val idx = items.indexOfFirst { it.produtoId == item.produtoId }
        if (idx >= 0) {
            items[idx].quantidade += item.quantidade
        } else {
            items.add(item)
        }
        save(context)
    }

    fun remove(context: Context, produtoId: String) {
        val idx = items.indexOfFirst { it.produtoId == produtoId }
        if (idx >= 0) {
            items.removeAt(idx)
            save(context)
        }
    }

    fun clear(context: Context) {
        items.clear()
        save(context)
    }

    fun all(): List<CartItem> = items.toList()
    fun total(): Double = items.sumOf { it.preco * it.quantidade }
}
