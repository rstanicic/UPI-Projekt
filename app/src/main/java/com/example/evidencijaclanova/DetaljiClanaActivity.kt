package com.example.evidencijaclanova

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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

        // Osnovni podaci
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

        // Članarina
        val tvStatusClanarine =
            findViewById<TextView>(R.id.tv_status_clanarine)

        val spinnerTip =
            findViewById<Spinner>(R.id.spinner_tip)

        val etIznos =
            findViewById<TextInputEditText>(R.id.et_iznos)

        val etDatumUplate =
            findViewById<TextInputEditText>(R.id.et_datum_uplate)

        val etDatumIsteka =
            findViewById<TextInputEditText>(R.id.et_datum_isteka)

        val cbClaarinaPlacena =
            findViewById<CheckBox>(R.id.cb_clanarina_placena)


        // NOVO - način plaćanja
        val spinnerNacinPlacanja =
            findViewById<Spinner>(R.id.spinner_nacin_placanja)

        val btnPlatiClanarinu =
            findViewById<MaterialButton>(R.id.btn_plati_clanarinu)


        // Tipovi članarine
        val tipovi = listOf(
            "godišnja",
            "polugodišnja",
            "mjesečna"
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tipovi
        )

        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTip.adapter = spinnerAdapter

        spinnerTip.setSelection(
            tipovi.indexOf(clan.tipClanarine)
                .takeIf { it >= 0 } ?: 0
        )


        // NAČINI PLAĆANJA
        val naciniPlacanja = listOf(
            "Kartica"
        )

        val adapterPlacanja = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            naciniPlacanja
        )

        adapterPlacanja.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerNacinPlacanja.adapter = adapterPlacanja


        // Popunjavanje podataka
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


        // Podaci članarine
        val (statusTekst, statusBoja) =
            ClanAdapter.izracunajStatus(clan)

        tvStatusClanarine.text = statusTekst
        tvStatusClanarine.setTextColor(statusBoja)

        etIznos.setText(
            if (clan.iznosClanarine > 0) {
                clan.iznosClanarine.toString()
            } else {
                ""
            }
        )

        etDatumUplate.setText(clan.datumUplate)
        etDatumIsteka.setText(clan.datumIsteka)

        cbClaarinaPlacena.isChecked =
            clan.platioClanarinu


        // Sinkronizacija checkboxeva
        cbClanarina.setOnCheckedChangeListener { _, isChecked ->
            cbClaarinaPlacena.isChecked = isChecked
        }

        cbClaarinaPlacena.setOnCheckedChangeListener { _, isChecked ->
            cbClanarina.isChecked = isChecked
        }


        // GUMB ZA PLAĆANJE
        btnPlatiClanarinu.setOnClickListener {

            val nacinPlacanja =
                spinnerNacinPlacanja.selectedItem.toString()

            if (nacinPlacanja == "Kartica") {

                prikaziProzorPlacanjaKarticom(clanId)

            } else {

                Toast.makeText(
                    this,
                    "Odabrano plaćanje gotovinom.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        // Prava pristupa
        val mozeEditirati =
            Session.isAdmin ||
                    Session.currentClan?.email == clan.email

        if (mozeEditirati) {

            toolbar.title =
                if (
                    Session.isAdmin &&
                    Session.currentClan?.email != clan.email
                ) {
                    "Uredi člana"
                } else {
                    "Moj profil"
                }

            btnSpremi.visibility = View.VISIBLE

            btnObrisi.visibility =
                if (Session.isAdmin) {
                    View.VISIBLE
                } else {
                    View.GONE
                }


            // Samo admin može ručno mijenjati podatke članarine
            if (!Session.isAdmin) {

                spinnerTip.isEnabled = false
                etIznos.isEnabled = false
                etDatumUplate.isEnabled = false
                etDatumIsteka.isEnabled = false

                cbClaarinaPlacena.isEnabled = false
                cbClanarina.isEnabled = false
            }


            btnSpremi.setOnClickListener {

                val novaLozinka =
                    etLozinka.text.toString().trim()

                val ime =
                    etIme.text.toString().trim()

                val prezime =
                    etPrezime.text.toString().trim()

                if (ime.isEmpty() || prezime.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Ime i prezime su obavezni.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                val spol = when (
                    rgSpol.checkedRadioButtonId
                ) {

                    R.id.rb_muski -> "M"

                    R.id.rb_zenski -> "Ž"

                    else -> clan.spol
                }

                val iznos =
                    etIznos.text
                        .toString()
                        .trim()
                        .toDoubleOrNull()
                        ?: 0.0

                val updatedClan = clan.copy(

                    ime = ime,

                    prezime = prezime,

                    oib =
                        etOib.text.toString().trim(),

                    datumRodjenja =
                        etDatum.text.toString().trim(),

                    telefon =
                        etTelefon.text.toString().trim(),

                    email =
                        etEmail.text.toString().trim(),

                    lozinka =
                        if (novaLozinka.isNotEmpty()) {
                            novaLozinka
                        } else {
                            clan.lozinka
                        },

                    opis =
                        etOpis.text.toString().trim(),

                    spol = spol,

                    aktivan =
                        cbAktivan.isChecked,

                    platioClanarinu =
                        cbClaarinaPlacena.isChecked,

                    tipClanarine =
                        spinnerTip.selectedItem.toString(),

                    iznosClanarine =
                        iznos,

                    datumUplate =
                        etDatumUplate.text
                            .toString()
                            .trim(),

                    datumIsteka =
                        etDatumIsteka.text
                            .toString()
                            .trim()
                )

                db.clanDao().update(updatedClan)


                if (Session.currentClan?.id == clan.id) {
                    Session.currentClan = updatedClan
                }


                Toast.makeText(
                    this,
                    "Promjene spremljene!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }


            btnObrisi.setOnClickListener {

                AlertDialog.Builder(this)

                    .setTitle("Obriši člana")

                    .setMessage(
                        "Jesi li siguran da želiš obrisati " +
                                "${clan.ime} ${clan.prezime}?"
                    )

                    .setPositiveButton(
                        "Obriši"
                    ) { _, _ ->

                        db.clanDao().delete(clan.id)

                        Toast.makeText(
                            this,
                            "Član obrisan",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }

                    .setNegativeButton(
                        "Odustani",
                        null
                    )

                    .show()
            }

        } else {

            // Tuđi profil - samo pregled

            toolbar.title =
                "${clan.ime} ${clan.prezime}"

            val svaPolja = listOf(
                etIme,
                etPrezime,
                etOib,
                etDatum,
                etTelefon,
                etEmail,
                etLozinka,
                etOpis,
                etIznos,
                etDatumUplate,
                etDatumIsteka
            )

            svaPolja.forEach {
                it.isEnabled = false
            }

            rbMuski.isEnabled = false
            rbZenski.isEnabled = false

            cbAktivan.isEnabled = false
            cbClanarina.isEnabled = false
            cbClaarinaPlacena.isEnabled = false

            spinnerTip.isEnabled = false

            spinnerNacinPlacanja.visibility = View.GONE
            btnPlatiClanarinu.visibility = View.GONE

            // Sakrij privatne podatke — OIB i podaci kartice
            findViewById<TextInputLayout>(R.id.til_oib)?.visibility = View.GONE
            findViewById<TextInputLayout>(R.id.til_lozinka)?.visibility = View.GONE

            btnSpremi.visibility = View.GONE
            btnObrisi.visibility = View.GONE
        }
    }


    // PROZOR ZA PLAĆANJE KARTICOM
    private fun prikaziProzorPlacanjaKarticom(clanId: Int) {

        val prikaz =
            layoutInflater.inflate(
                R.layout.dialog_placanje_karticom,
                null
            )

        val etImeNaKartici =
            prikaz.findViewById<EditText>(
                R.id.etImeNaKartici
            )

        val etBrojKartice =
            prikaz.findViewById<EditText>(
                R.id.etBrojKartice
            )

        val etDatumIstekaKartice =
            prikaz.findViewById<EditText>(
                R.id.etDatumIsteka
            )

        val etCvv =
            prikaz.findViewById<EditText>(
                R.id.etCvv
            )


        val btnOdustani = prikaz.findViewById<MaterialButton>(R.id.btn_odustani_kartica)
        val btnPlatiDialog = prikaz.findViewById<MaterialButton>(R.id.btn_plati_kartica)

        val dialog =
            AlertDialog.Builder(this)
                .setView(prikaz)
                .create()

        btnOdustani.setOnClickListener { dialog.dismiss() }

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnPlatiDialog.setOnClickListener {


                val ime =
                    etImeNaKartici.text
                        .toString()
                        .trim()

                val brojKartice =
                    etBrojKartice.text
                        .toString()
                        .trim()

                val datumIsteka =
                    etDatumIstekaKartice.text
                        .toString()
                        .trim()

                val cvv =
                    etCvv.text
                        .toString()
                        .trim()


                when {

                    ime.isEmpty() -> {

                        etImeNaKartici.error =
                            "Unesite ime i prezime"
                    }


                    brojKartice.length != 16 -> {

                        etBrojKartice.error =
                            "Broj kartice mora imati 16 znamenki"
                    }


                    datumIsteka.length != 5 ||
                            !datumIsteka.contains("/") -> {

                        etDatumIstekaKartice.error =
                            "Unesite datum u obliku MM/GG"
                    }


                    cvv.length != 3 -> {

                        etCvv.error =
                            "CVV mora imati 3 znamenke"
                    }


                    else -> {
                        val maskirani = "**** **** **** ${brojKartice.takeLast(4)}"
                        val updatedClan = db.clanDao().getById(clanId)
                        if (updatedClan != null) {
                            val saved = updatedClan.copy(
                                nacinPlacanja = "Kartica",
                                imeNaKartici = ime,
                                maskiraniBrojKartice = maskirani
                            )
                            db.clanDao().update(saved)
                            if (Session.currentClan?.id == clanId) {
                                Session.currentClan = saved
                            }
                        }
                        dialog.dismiss()
                        Toast.makeText(this, "✅ Podaci kartice spremljeni!", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        dialog.show()


        val sirina =
            (
                    resources.displayMetrics.widthPixels
                            * 0.85
                    ).toInt()


        dialog.window?.setLayout(
            sirina,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onSupportNavigateUp(): Boolean {

        finish()

        return true
    }
}