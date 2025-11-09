package com.example.projeto_ecommerce

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class CadastroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        val spinnerTipoUsuario: Spinner = findViewById(R.id.spinnerTipoUsuario)

        ArrayAdapter.createFromResource(
            this,
            R.array.tipos_usuario, // O nosso array com "Cliente" e "Vendedor"
            android.R.layout.simple_spinner_item
        ).also { adapter ->

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerTipoUsuario.adapter = adapter
        }

    }
}
