package com.example.evidencijaclanova

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        val emailField = findViewById<TextInputEditText>(R.id.et_email)
        val passwordField = findViewById<TextInputEditText>(R.id.et_password)
        val loginButton = findViewById<MaterialButton>(R.id.btn_login)
        val btnRegistracija = findViewById<MaterialButton>(R.id.btn_registracija)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            val clan = db.clanDao().login(email, password)
            if (clan != null) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, "Krivi email ili lozinka", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegistracija.setOnClickListener {
            startActivity(Intent(this, RegistracijaClanova::class.java))
        }
    }
}