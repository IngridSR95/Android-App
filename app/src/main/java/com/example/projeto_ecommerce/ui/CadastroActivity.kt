package com.example.projeto_ecommerce.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projeto_ecommerce.databinding.ActivityCadastroBinding
import com.example.projeto_ecommerce.model.PerfilUsuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "CadastroActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        val tiposUsuario = resources.getStringArray(com.example.projeto_ecommerce.R.array.tipos_usuario)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposUsuario)
        binding.autoCompleteTipoUsuario.setAdapter(adapter)

        binding.buttonCadastrar.setOnClickListener { cadastrar() }
    }

    private fun cadastrar() {
        if (!isOnline()) {
            Toast.makeText(this, "Sem conexão de internet", Toast.LENGTH_SHORT).show()
            return
        }

        val nome = binding.editTextNomeCompleto.text.toString().trim()
        val cpfCnpj = binding.editTextCpfCnpj.text.toString().trim()
        val dataNasc = binding.editTextDataNascimento.text.toString().trim()
        val cep = binding.editTextCep.text.toString().trim()
        val estado = binding.editTextEstado.text.toString().trim()
        val cidade = binding.editTextCidade.text.toString().trim()
        val bairro = binding.editTextBairro.text.toString().trim()
        val rua = binding.editTextRua.text.toString().trim()
        val email = binding.editTextEmailCadastro.text.toString().trim()
        val senha = binding.editTextSenhaCadastro.text.toString()
        val tipo = binding.autoCompleteTipoUsuario.text.toString().trim()

        // Validações básicas
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha nome, email e senha", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputLayoutEmail.error = "Email inválido"
            return
        } else {
            binding.inputLayoutEmail.error = null
        }
        if (senha.length < 6) {
            binding.inputLayoutSenha.error = "Senha deve ter ao menos 6 caracteres"
            return
        } else {
            binding.inputLayoutSenha.error = null
        }

        // desabilita botão para evitar múltiplos cliques
        binding.buttonCadastrar.isEnabled = false

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrEmpty()) {
                    Log.e(TAG, "UID nulo após createUser")
                    Toast.makeText(this, "Erro ao criar usuário (uid vazio)", Toast.LENGTH_SHORT).show()
                    binding.buttonCadastrar.isEnabled = true
                    return@addOnSuccessListener
                }

                val profile = PerfilUsuario(
                    uid = uid,
                    nome = nome,
                    cpfCnpj = cpfCnpj,
                    dataNascimento = dataNasc,
                    cep = cep,
                    estado = estado,
                    cidade = cidade,
                    bairro = bairro,
                    rua = rua,
                    email = email,
                    tipoUsuario = tipo
                )

                db.collection("Usuarios").document(uid).set(profile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cadastro realizado", Toast.LENGTH_SHORT).show()

                        if (tipo.equals("Vendedor", ignoreCase = true)) {
                            // Mantemos o usuário logado e redirecionamos para criar a loja
                            val i = Intent(this, CriarLojaActivity::class.java)
                            i.putExtra("ownerUid", uid)
                            startActivity(i)
                            finish()
                        } else {
                            // desloga e volta ao login, pulando auto-redirect
                            auth.signOut()
                            val i = Intent(this, MainActivity::class.java)
                            i.putExtra("skipAutoLogin", true)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(i)
                            // finish() não necessário, CLEAR_TASK já cuida
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Erro ao salvar perfil", e)
                        Toast.makeText(this, "Erro ao salvar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.buttonCadastrar.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro no createUser", e)
                val msg = e.message ?: "Erro no cadastro"
                Toast.makeText(this, "Erro no cadastro: $msg", Toast.LENGTH_LONG).show()
                binding.buttonCadastrar.isEnabled = true
            }

    }

    // Recurso reaproveitado da MainActivity para checar conexão
    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(n) ?: return false
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            val info = cm.activeNetworkInfo ?: return false
            return info.isConnected
        }

    }
}
