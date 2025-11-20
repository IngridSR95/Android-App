package com.example.projeto_ecommerce.model

data class PerfilUsuario(
    val uid: String = "",
    val nome: String = "",
    val cpfCnpj: String = "",
    val dataNascimento: String = "",
    val cep: String = "",
    val estado: String = "",
    val cidade: String = "",
    val bairro: String = "",
    val rua: String = "",
    val email: String = "",
    val tipoUsuario: String = ""
)
