package com.example.projeto_ecommerce.model

import java.io.Serializable

data class Produto(
    var id: String = "",
    var titulo: String = "",
    var preco: Double = 0.0,
    var descricao: String = "",
    var imagem: String = "",
    var storeId: String? = null
) : Serializable
