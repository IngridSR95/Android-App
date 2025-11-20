package com.example.projeto_ecommerce.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.projeto_ecommerce.databinding.ActivityCriarLojaBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CriarLojaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriarLojaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null
    private val TAG = "CriarLojaActivity"

    // Launcher para escolher a imagem
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // atualiza preview (ImageView id = imgLoja no seu XML)
            binding.imgLoja.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarLojaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // clique para escolher imagem
        binding.btnEscolherImagem.setOnClickListener { imagePicker.launch("image/*") }
        binding.imgLoja.setOnClickListener { imagePicker.launch("image/*") }

        binding.btnSalvarLoja.setOnClickListener {
            binding.btnSalvarLoja.isEnabled = false
            salvarLoja()
        }
    }

    private fun salvarLoja() {
        val nome = binding.etNomeLoja.text.toString().trim()
        val descricao = binding.etDescricaoLoja.text.toString().trim()

        if (nome.isEmpty()) {
            // mostra erro no TextInputLayout (id = layoutNomeLoja)
            binding.layoutNomeLoja.error = "Obrigatório"
            binding.btnSalvarLoja.isEnabled = true
            return
        } else {
            binding.layoutNomeLoja.error = null
        }

        binding.progressUpload.visibility = View.VISIBLE
        binding.progressUpload.progress = 0

        // ownerUid pode ser passado via Intent (por exemplo, admin criando para outro vendedor)
        val uidFromIntent = intent.getStringExtra("ownerUid").orEmpty()
        val uidActual = auth.currentUser?.uid.orEmpty()
        val ownerUid = if (uidFromIntent.isNotEmpty()) uidFromIntent else uidActual

        if (selectedImageUri != null) {
            uploadImagem(ownerUid, nome, descricao)
        } else {
            // sem imagem, salva direto
            saveLojaToFirestore(imageUrl = null, ownerUid = ownerUid, nome = nome, descricao = descricao)
        }
    }

    private fun uploadImagem(ownerUid: String, nome: String, descricao: String) {
        val fileName = "loja_${System.currentTimeMillis()}.jpg"
        val path = if (ownerUid.isNotEmpty()) "lojas/$ownerUid/$fileName" else "lojas/$fileName"
        val ref = storage.reference.child(path)

        selectedImageUri?.let { uri ->
            ref.putFile(uri)
                .addOnProgressListener { snap ->
                    val total = snap.totalByteCount
                    val transferred = snap.bytesTransferred
                    if (total > 0) {
                        val progress = ((transferred * 100) / total).toInt()
                        binding.progressUpload.progress = progress
                    }
                }
                .addOnSuccessListener { taskSnapshot ->
                    // pega downloadUrl a partir da reference retornada
                    val uploadedRef = taskSnapshot.storage
                    Log.d(TAG, "Upload concluído. path=${uploadedRef.path}")
                    uploadedRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            val url = downloadUri.toString()
                            Log.d(TAG, "downloadUrl obtida: $url")
                            saveLojaToFirestore(imageUrl = url, ownerUid = ownerUid, nome = nome, descricao = descricao)
                        }
                        .addOnFailureListener { e ->
                            erro("Erro ao obter URL da imagem: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    erro("Erro no upload da imagem: ${e.message}")
                }
        } ?: run {
            // safety fallback
            saveLojaToFirestore(imageUrl = null, ownerUid = ownerUid, nome = nome, descricao = descricao)
        }
    }

    private fun saveLojaToFirestore(imageUrl: String?, ownerUid: String, nome: String, descricao: String) {
        val collection = db.collection("lojas") // padronizado para minúsculo
        // se você quiser que cada vendedor tenha documento com id = ownerUid, mantém esse comportamento
        val docRef = if (ownerUid.isNotEmpty()) collection.document(ownerUid) else collection.document()

        val lojaMap = hashMapOf(
            "createdBy" to ownerUid,
            "nome" to nome,
            "descricao" to descricao,
            "imagem" to imageUrl,
            "createdAt" to Timestamp.now()
        )

        docRef.set(lojaMap)
            .addOnSuccessListener {
                val storeId = docRef.id
                // atualiza o próprio doc com storeId (ajuda para buscas / consistência)
                docRef.update("storeId", storeId)
                    .addOnSuccessListener {
                        // salva storeId nas prefs para uso posterior (ex: VendedorActivity)
                        saveStoreIdToPrefs(storeId)
                        binding.progressUpload.visibility = View.GONE
                        Toast.makeText(this, "Loja criada com sucesso!", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // mesmo se falhar atualizar o storeId, consideramos sucesso do create
                        binding.progressUpload.visibility = View.GONE
                        Log.w(TAG, "Loja criada, mas falha ao atualizar storeId: ${e.message}")
                        Toast.makeText(this, "Loja criada (sem storeId atualizado).", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                erro("Erro ao salvar loja: ${e.message}")
            }
    }

    private fun saveStoreIdToPrefs(storeId: String) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("storeId", storeId).apply()
        Log.d(TAG, "storeId salvo em prefs: $storeId")
    }

    private fun erro(msg: String) {
        Log.e(TAG, msg)
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        binding.btnSalvarLoja.isEnabled = true
        binding.progressUpload.visibility = View.GONE
    }
}
