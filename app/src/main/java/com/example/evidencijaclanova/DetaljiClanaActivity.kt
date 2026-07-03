package com.example.evidencijaclanova

import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class DetaljiClanaActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalji_clana)

        db = AppDatabase.getDatabase(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val clanId = intent.getIntExtra("clan_id", -1)
        val clan = db.clanDao().getById(clanId)

        if (clan == null) {
            finish()
            return
        }

        val tvAvatar = findViewById<TextView>(R.id.tv_avatar)
        val etIme = findViewById<TextInputEditText>(R.id.et_ime)
        val etPrezime = findViewById<TextInputEditText>(R.id.et_prezime)
        val etOib = findViewById<TextInputEditText>(R.id.et_oib)
        val etDatum = findViewById<TextInputEditText>(R.id.et_datum)
        val etTelefon = findViewById<TextInputEditText>(R.id.et_telefon)
        val etEmail = findViewById<TextInputEditText>(R.id.et_email)
        val etLozinka = findViewById<TextInputEditText>(R.id.et_lozinka)
        val etOpis = findViewById<TextInputEditText>(R.id.et_opis)
        val rgSpol = findViewById<RadioGroup>(R.id.rg_spol)
        val rbMuski = findViewById<RadioButton>(R.id.rb_muski)
        val rbZenski = findViewById<RadioButton>(R.id.rb_zenski)
        val cbAktivan = findViewById<CheckBox>(R.id.cb_aktivan)
        val cbClanarina = findViewById<CheckBox>(R.id.cb_clanarina)
        val btnSpremi = findViewById<MaterialButton>(R.id.btn_spremi)
        val btnObrisi = findViewById<MaterialButton>(R.id.btn_obrisi)

        tvAvatar.text = when (clan.spol) {
            "M" -> "👨"
            "Ž" -> "👩"
            else -> "👤"
        }

        etIme.setText(clan.ime)
        etPrezime.setText(clan.prezime)
        etOib.setText(clan.oib)
        etDatum.setText(clan.datumRodjenja)
        etTelefon.setText(clan.telefon)
        etEmail.setText(clan.email)
        etOpis.setText(clan.opis)
        when (clan.spol) {
            "M" -> rbMuski.isChecked = true
            "Ž" -> rbZenski.isChecked = true
        }
        cbAktivan.isChecked = clan.aktivan
        cbClanarina.isChecked = clan.platioClanarinu

        btnSpremi.setOnClickListener {
            val novaLozinka = etLozinka.text.toString().trim()
            val spol = when (rgSpol.checkedRadioButtonId) {
                R.id.rb_muski -> "M"
                R.id.rb_zenski -> "Ž"
                else -> clan.spol
            }

            val updatedClan = clan.copy(
                ime = etIme.text.toString().trim(),
                prezime = etPrezime.text.toString().trim(),
                oib = etOib.text.toString().trim(),
                datumRodjenja = etDatum.text.toString().trim(),
                telefon = etTelefon.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                lozinka = if (novaLozinka.isNotEmpty()) novaLozinka else clan.lozinka,
                opis = etOpis.text.toString().trim(),
                spol = spol,
                aktivan = cbAktivan.isChecked,
                platioClanarinu = cbClanarina.isChecked
            )
            db.clanDao().update(updatedClan)
            Toast.makeText(this, "Promjene spremljene!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnObrisi.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Obriši člana")
                .setMessage("Jesi li siguran da želiš obrisati ${clan.ime} ${clan.prezime}?")
                .setPositiveButton("Obriši") { _, _ ->
                    db.clanDao().delete(clan.id)
                    Toast.makeText(this, "Član obrisan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Odustani", null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}