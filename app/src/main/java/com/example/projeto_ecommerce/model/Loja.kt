package com.example.projeto_ecommerce.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class Loja(
    var id: String = "",
    var storeId: String? = null,
    var nome: String? = null,
    var descricao: String? = null,
    var imagem: String? = null,
    var createdBy: String? = null,
    var createdAt: Timestamp? = null
) : Serializable
