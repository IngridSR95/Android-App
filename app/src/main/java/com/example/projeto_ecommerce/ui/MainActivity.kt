package com.example.projeto_ecommerce.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projeto_ecommerce.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "MainActivity"

    // Se em alguma Intent quisermos pular o auto-redirect (útil no logout)
    private var allowAutoRedirect = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        // verifica se a Intent pediu explicitamente para pular o auto-login
        val skipFromIntent = intent.getBooleanExtra("skipAutoLogin", false)
        if (skipFromIntent) allowAutoRedirect = false

        binding.buttonLogin.setOnClickListener {
            if (!isOnline()) {
                Toast.makeText(this, "Sem conexão de internet", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Tentar logar sem conexão")
                return@setOnClickListener
            }

            val email = binding.editTextEmail.text.toString().trim()
            val senha = binding.editTextPassword.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.layoutEmail.error = "Email inválido"
                return@setOnClickListener
            } else {
                binding.layoutEmail.error = null
            }

            binding.buttonLogin.isEnabled = false
            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid == null) {
                        Toast.makeText(this, "Erro ao obter usuário", Toast.LENGTH_SHORT).show()
                        binding.buttonLogin.isEnabled = true
                        return@addOnSuccessListener
                    }
                    // após login explícito via botão, um redirect imediato é esperado:
                    redirectBasedOnType(uid)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao logar", e)
                    val msg = e.message ?: "Erro no login"
                    Toast.makeText(this, "Falha no login: $msg", Toast.LENGTH_LONG).show()
                    binding.buttonLogin.isEnabled = true
                }
        }

        binding.textViewCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    /**
     * Redireciona o usuário de acordo com o tipo salvo no documento Usuarios/{uid}
     * se for Vendedor, também busca a loja associada e salva o storeId nas prefs.
     */
    private fun redirectBasedOnType(uid: String) {
        db.collection("Usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val tipo = doc.getString("tipoUsuario") ?: ""
                Log.d(TAG, "Tipo usuário: $tipo (uid=$uid)")
                if (tipo.equals("Vendedor", ignoreCase = true)) {
                    // busca a loja do vendedor e salva em prefs; redireciona conforme tiver loja
                    fetchUserStoreAndSaveToPrefs(uid) { storeId ->
                        binding.buttonLogin.isEnabled = true
                        if (storeId != null) {
                            // vendedor com loja -> abrir painel vendedor
                            startActivity(Intent(this, VendedorActivity::class.java))
                        } else {
                            // vendedor sem loja -> abrir criar loja
                            startActivity(Intent(this, CriarLojaActivity::class.java))
                        }
                        finish()
                    }
                } else {
                    // usuário comum -> abrir tela de produtos
                    binding.buttonLogin.isEnabled = true
                    startActivity(Intent(this, LojasActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao buscar perfil", e)
                binding.buttonLogin.isEnabled = true
                // fallback para tela normal
                startActivity(Intent(this, LojasActivity::class.java))
                finish()
            }
    }

    /**
     * Busca a loja associada ao usuário (collection "lojas" onde createdBy == uid).
     * Se encontrar, salva o storeId em SharedPreferences ("app_prefs" / "storeId") e chama onComplete(storeId).
     * Se não encontrar, chama onComplete(null).
     */
    private fun fetchUserStoreAndSaveToPrefs(uid: String, onComplete: (storeId: String?) -> Unit) {
        db.collection("lojas")
            .whereEqualTo("createdBy", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val storeId = doc.getString("storeId") ?: doc.id
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    prefs.edit().putString("storeId", storeId).apply()
                    Log.d(TAG, "StoreId salvo em prefs: $storeId")
                    onComplete(storeId)
                } else {
                    Log.d(TAG, "Nenhuma loja encontrada para uid=$uid")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao buscar loja do usuário: ${e.message}", e)
                onComplete(null)
            }
    }

    // Função de logout útil para testes e para o botão "Sair"
    private fun doLogout() {
        auth.signOut()
        // ao reenviar para a MainActivity pedimos explicitamente para pular o auto-login
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        i.putExtra("skipAutoLogin", true)
        startActivity(i)
        finish()
    }

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
