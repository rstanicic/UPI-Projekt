package com.example.evidencijaclanova

import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegistracijaClanova : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registracija_clanova)

        db = AppDatabase.getDatabase(this)

        val tvNaslov = findViewById<TextView>(R.id.tv_naslov)
        val etIme = findViewById<TextInputEditText>(R.id.et_ime)
        val etPrezime = findViewById<TextInputEditText>(R.id.et_prezime)
        val etOib = findViewById<TextInputEditText>(R.id.et_oib)
        val etDatum = findViewById<TextInputEditText>(R.id.et_datum)
        val etTelefon = findViewById<TextInputEditText>(R.id.et_telefon)
        val etEmail = findViewById<TextInputEditText>(R.id.et_email)
        val etLozinka = findViewById<TextInputEditText>(R.id.et_lozinka)
        val etOpis = findViewById<TextInputEditText>(R.id.et_opis)
        val rgSpol = findViewById<RadioGroup>(R.id.rg_spol)
        val btnRegistracija = findViewById<MaterialButton>(R.id.btn_registracija)
        val btnNazad = findViewById<MaterialButton>(R.id.btn_nazad)

        val dolazimIzClanovi = intent.getBooleanExtra("iz_clanovi", false)

        if (dolazimIzClanovi) {
            tvNaslov.text = "Dodaj člana"
            btnNazad.text = "Odustani"
            btnRegistracija.text = "Dodaj člana"
        } else {
            tvNaslov.text = "Registracija"
            btnNazad.text = "Već imam račun"
        }

        btnRegistracija.setOnClickListener {
            val ime = etIme.text.toString().trim()
            val prezime = etPrezime.text.toString().trim()
            val oib = etOib.text.toString().trim()
            val datum = etDatum.text.toString().trim()
            val telefon = etTelefon.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val lozinka = etLozinka.text.toString().trim()
            val opis = etOpis.text.toString().trim()
            val spol = when (rgSpol.checkedRadioButtonId) {
                R.id.rb_muski -> "M"
                R.id.rb_zenski -> "Ž"
                else -> ""
            }

            if (ime.isEmpty() || prezime.isEmpty() || email.isEmpty() || lozinka.isEmpty()) {
                Toast.makeText(this, "Molimo ispunite obavezna polja", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (oib.isNotEmpty() && oib.length != 11) {
                Toast.makeText(this, "OIB mora imati 11 znamenki", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val noviClan = Clan(
                ime = ime,
                prezime = prezime,
                oib = oib,
                datumRodjenja = datum,
                telefon = telefon,
                email = email,
                lozinka = lozinka,
                opis = opis,
                spol = spol
            )

            db.clanDao().insert(noviClan)
            Toast.makeText(this, "Član uspješno dodan!", Toast.LENGTH_SHORT).show()

            if (dolazimIzClanovi) {
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        btnNazad.setOnClickListener {
            if (dolazimIzClanovi) {
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}