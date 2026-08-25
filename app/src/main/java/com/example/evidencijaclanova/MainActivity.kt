package com.example.evidencijaclanova

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    companion object {
        const val ADMIN_EMAIL = "admin@admin.com"
        const val ADMIN_PASSWORD = "admin123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        // Ako je korisnik već prijavljen (Session još živi u memoriji), preskoči login
        if (Session.isAdmin || Session.currentClan != null) {
            startActivity(Intent(this, PostavkeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            return
        }

        val emailField = findViewById<TextInputEditText>(R.id.et_email)
        val passwordField = findViewById<TextInputEditText>(R.id.et_password)
        val loginButton = findViewById<MaterialButton>(R.id.btn_login)
        val btnRegistracija = findViewById<MaterialButton>(R.id.btn_registracija)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            when {
                // Admin login — hardkodirani račun
                email == ADMIN_EMAIL && password == ADMIN_PASSWORD -> {
                    Session.isAdmin = true
                    Session.currentClan = null

                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                }

                // Regularni član
                else -> {
                    val clan = db.clanDao().login(email, password)

                    if (clan != null) {
                        Session.isAdmin = false
                        Session.currentClan = clan

                        if (!clan.platioClanarinu) {
                            prikaziPopupClanarine(clan.id)
                        } else {
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                        }

                    } else {
                        Toast.makeText(
                            this,
                            "Krivi email ili lozinka",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        btnRegistracija.setOnClickListener {
            startActivity(Intent(this, RegistracijaClanova::class.java))
        }
    }

    private fun prikaziPopupClanarine(clanId: Int) {

        val prikaz = layoutInflater.inflate(
            R.layout.dialog_clanarina_nije_placena,
            null
        )

        val btnKasnije =
            prikaz.findViewById<MaterialButton>(R.id.btn_kasnije)

        val btnPlati =
            prikaz.findViewById<MaterialButton>(R.id.btn_plati_clanarinu)

        val dialog = AlertDialog.Builder(this)
            .setView(prikaz)
            .create()

        dialog.setCancelable(false)

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(
                android.R.color.transparent
            )
        }

        btnKasnije.setOnClickListener {
            dialog.dismiss()

            val intent = Intent(this, HomeActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        btnPlati.setOnClickListener {
            dialog.dismiss()

            // Otvori HomeActivity + ClanarinaActivity zajedno
            // da back navigacija vodi na Home, ne na Login
            val homeIntent = Intent(this, HomeActivity::class.java)
            val clanarinaIntent = Intent(this, ClanarinaActivity::class.java)
            clanarinaIntent.putExtra("clan_id", clanId)
            startActivities(arrayOf(homeIntent, clanarinaIntent))
        }

        dialog.show()

        val sirina =
            (resources.displayMetrics.widthPixels * 0.85).toInt()

        dialog.window?.setLayout(
            sirina,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}