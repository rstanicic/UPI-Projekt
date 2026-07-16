package com.example.evidencijaclanova

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class ClanarinaActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var selectedPlan: String = "godišnja"
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    companion object {
        const val CIJENA_MJESECNA = 10.0
        const val CIJENA_POLUGODISNJA = 55.0
        const val CIJENA_GODISNJA = 100.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clanarina)

        db = AppDatabase.getDatabase(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val clanId = intent.getIntExtra("clan_id", -1)
        var clan = db.clanDao().getById(clanId) ?: run { finish(); return }

        val isAdmin = Session.isAdmin
        val isOwn = Session.currentClan?.id == clan.id

        // Views
        val tvStatusEmoji = findViewById<TextView>(R.id.tv_status_emoji)
        val tvStatusTekst = findViewById<TextView>(R.id.tv_status_tekst)
        val tvPlanNaziv = findViewById<TextView>(R.id.tv_plan_naziv)
        val tvDanaOstalo = findViewById<TextView>(R.id.tv_dana_ostalo)
        val layoutPlanovi = findViewById<View>(R.id.layout_planovi)
        val cardDetalji = findViewById<MaterialCardView>(R.id.card_detalji)
        val cardAdmin = findViewById<MaterialCardView>(R.id.card_admin)
        val tvIznos = findViewById<TextView>(R.id.tv_iznos)
        val tvDatumUplate = findViewById<TextView>(R.id.tv_datum_uplate)
        val tvDatumIsteka = findViewById<TextView>(R.id.tv_datum_isteka)
        val tvNacinPlacanja = findViewById<TextView>(R.id.tv_nacin_placanja)
        val btnPlati = findViewById<MaterialButton>(R.id.btn_plati)
        val btnPonisti = findViewById<MaterialButton>(R.id.btn_ponisti)
        val spinnerNacin = findViewById<Spinner>(R.id.spinner_nacin)
        val spinnerNacinMember = findViewById<Spinner>(R.id.spinner_nacin_member)
        val layoutNacinPlacanja = findViewById<View>(R.id.layout_nacin_placanja)
        val etAdminDatumUplate = findViewById<TextInputEditText>(R.id.et_admin_datum_uplate)
        val etAdminDatumIsteka = findViewById<TextInputEditText>(R.id.et_admin_datum_isteka)
        val btnAdminSpremi = findViewById<MaterialButton>(R.id.btn_admin_spremi)
        val cardPlanMj = findViewById<MaterialCardView>(R.id.card_plan_mjesecna)
        val cardPlanPol = findViewById<MaterialCardView>(R.id.card_plan_polugodisnja)
        val cardPlanGod = findViewById<MaterialCardView>(R.id.card_plan_godisnja)

        // Tekstovi unutar plan kartica
        val tvMjNaziv = findViewById<TextView>(R.id.tv_mj_naziv)
        val tvMjCijena = findViewById<TextView>(R.id.tv_mj_cijena)
        val tvMjOpis = findViewById<TextView>(R.id.tv_mj_opis)
        val tvPolNaziv = findViewById<TextView>(R.id.tv_pol_naziv)
        val tvPolCijena = findViewById<TextView>(R.id.tv_pol_cijena)
        val tvPolOpis = findViewById<TextView>(R.id.tv_pol_opis)
        val tvGodNaziv = findViewById<TextView>(R.id.tv_god_naziv)
        val tvGodCijena = findViewById<TextView>(R.id.tv_god_cijena)
        val tvGodOpis = findViewById<TextView>(R.id.tv_god_opis)

        // Postavi naslov
        toolbar.title = if (isOwn) "Moja članarina" else "Članarina — ${clan.ime} ${clan.prezime}"

        fun osvjeziStatus() {
            clan = db.clanDao().getById(clanId) ?: return
            val (statusTekst, _) = ClanAdapter.izracunajStatus(clan)

            when {
                statusTekst.startsWith("✅") -> {
                    tvStatusEmoji.text = "✅"
                    tvStatusTekst.text = "Plaćeno"
                    tvStatusTekst.setTextColor(ContextCompat.getColor(this, R.color.active_green))
                }
                statusTekst.startsWith("⚠️") -> {
                    tvStatusEmoji.text = "⚠️"
                    tvStatusTekst.text = "Ističe uskoro"
                    tvStatusTekst.setTextColor(0xFFFF8F00.toInt())
                }
                statusTekst.startsWith("❌") -> {
                    tvStatusEmoji.text = "❌"
                    tvStatusTekst.text = "Isteklo"
                    tvStatusTekst.setTextColor(ContextCompat.getColor(this, R.color.inactive_red))
                }
                else -> {
                    tvStatusEmoji.text = "💔"
                    tvStatusTekst.text = "Nije platio"
                    tvStatusTekst.setTextColor(ContextCompat.getColor(this, R.color.inactive_red))
                }
            }

            tvPlanNaziv.text = clan.tipClanarine.replaceFirstChar { it.uppercase() } + " članarina"

            // Dani ostalo
            if (clan.platioClanarinu && clan.datumIsteka.isNotEmpty()) {
                try {
                    val istekDatum = sdf.parse(clan.datumIsteka)
                    val razlikaDana = ((istekDatum!!.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                    tvDanaOstalo.text = when {
                        razlikaDana > 0 -> "Još $razlikaDana dana (do ${clan.datumIsteka})"
                        else -> "Isteklo ${clan.datumIsteka}"
                    }
                } catch (e: Exception) {
                    tvDanaOstalo.text = "Plaćeno"
                }
            } else if (!clan.platioClanarinu) {
                tvDanaOstalo.text = "Članarina nije aktivna"
            } else {
                tvDanaOstalo.text = ""
            }

            // Detalji
            tvIznos.text = "💶 Iznos: ${if (clan.iznosClanarine > 0) "${clan.iznosClanarine}€" else "—"}"
            tvDatumUplate.text = "📅 Datum uplate: ${clan.datumUplate.ifEmpty { "—" }}"
            tvDatumIsteka.text = "⏳ Datum isteka: ${clan.datumIsteka.ifEmpty { "—" }}"
            tvNacinPlacanja.text = "💳 Način plaćanja: ${clan.nacinPlacanja}"
        }

        osvjeziStatus()

        // Odabir plana — vidljiv vlasniku i adminu
        if (isOwn || isAdmin) {
            layoutPlanovi.visibility = View.VISIBLE
            cardDetalji.visibility = View.VISIBLE
            layoutNacinPlacanja.visibility = View.VISIBLE
            btnPlati.visibility = View.VISIBLE
            if (clan.platioClanarinu) btnPonisti.visibility = View.VISIBLE

            selectedPlan = clan.tipClanarine

            // Spinner za način plaćanja (vlastiti + admin)
            val nacinOptionsMember = listOf("Gotovina", "Kartica")
            val nacinMemberAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nacinOptionsMember)
            nacinMemberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerNacinMember.adapter = nacinMemberAdapter
            val memberNacinIndex = nacinOptionsMember.indexOf(clan.nacinPlacanja).takeIf { it >= 0 } ?: 0
            spinnerNacinMember.setSelection(memberNacinIndex)

            val colorSelected = ContextCompat.getColor(this, R.color.primary)
            val colorDefault = ContextCompat.getColor(this, R.color.card)
            val colorWhite = ContextCompat.getColor(this, R.color.white)
            val colorTextDark = ContextCompat.getColor(this, R.color.text_dark)
            val colorTextGray = ContextCompat.getColor(this, R.color.text_gray)
            val colorPrimary = ContextCompat.getColor(this, R.color.primary)

            fun bojajTekstove(
                naziv: TextView, cijena: TextView, opis: TextView, selected: Boolean
            ) {
                if (selected) {
                    naziv.setTextColor(colorWhite)
                    cijena.setTextColor(colorWhite)
                    opis.setTextColor(colorWhite)
                } else {
                    naziv.setTextColor(colorTextDark)
                    cijena.setTextColor(colorPrimary)
                    opis.setTextColor(colorTextGray)
                }
            }

            fun osvjeziPlanKartice() {
                cardPlanMj.setCardBackgroundColor(if (selectedPlan == "mjesečna") colorSelected else colorDefault)
                cardPlanPol.setCardBackgroundColor(if (selectedPlan == "polugodišnja") colorSelected else colorDefault)
                cardPlanGod.setCardBackgroundColor(if (selectedPlan == "godišnja") colorSelected else colorDefault)

                bojajTekstove(tvMjNaziv, tvMjCijena, tvMjOpis, selectedPlan == "mjesečna")
                bojajTekstove(tvPolNaziv, tvPolCijena, tvPolOpis, selectedPlan == "polugodišnja")
                bojajTekstove(tvGodNaziv, tvGodCijena, tvGodOpis, selectedPlan == "godišnja")
            }
            osvjeziPlanKartice()

            cardPlanMj.setOnClickListener { selectedPlan = "mjesečna"; osvjeziPlanKartice() }
            cardPlanPol.setOnClickListener { selectedPlan = "polugodišnja"; osvjeziPlanKartice() }
            cardPlanGod.setOnClickListener { selectedPlan = "godišnja"; osvjeziPlanKartice() }

            btnPlati.setOnClickListener {
                val calendar = Calendar.getInstance()
                val iznos = when (selectedPlan) {
                    "mjesečna" -> { calendar.add(Calendar.MONTH, 1); CIJENA_MJESECNA }
                    "polugodišnja" -> { calendar.add(Calendar.MONTH, 6); CIJENA_POLUGODISNJA }
                    else -> { calendar.add(Calendar.YEAR, 1); CIJENA_GODISNJA }
                }
                val datumUplate = sdf.format(Date())
                val datumIsteka = sdf.format(calendar.time)
                val odabraniNacin = spinnerNacinMember.selectedItem.toString()

                val updated = clan.copy(
                    platioClanarinu = true,
                    tipClanarine = selectedPlan,
                    iznosClanarine = iznos,
                    datumUplate = datumUplate,
                    datumIsteka = datumIsteka,
                    nacinPlacanja = odabraniNacin
                )
                db.clanDao().update(updated)
                if (isOwn) Session.currentClan = updated
                Toast.makeText(this, "✅ Uplata evidentirana!", Toast.LENGTH_SHORT).show()
                btnPonisti.visibility = View.VISIBLE
                osvjeziStatus()
            }

            btnPonisti.setOnClickListener {
                val updated = clan.copy(
                    platioClanarinu = false,
                    datumUplate = "",
                    datumIsteka = "",
                    iznosClanarine = 0.0
                )
                db.clanDao().update(updated)
                if (isOwn) Session.currentClan = updated
                Toast.makeText(this, "Uplata poništena", Toast.LENGTH_SHORT).show()
                btnPonisti.visibility = View.GONE
                osvjeziStatus()
            }

        } else {
            // Tuđi profil — samo status i plan, bez detalja i gumba
            layoutPlanovi.visibility = View.GONE
            cardDetalji.visibility = View.GONE
            btnPlati.visibility = View.GONE
            btnPonisti.visibility = View.GONE
        }

        // Admin: ručno uređivanje
        if (isAdmin) {
            cardAdmin.visibility = View.VISIBLE

            val nacinOptions = listOf("Gotovina", "Kartica", "Bankovni transfer")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nacinOptions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerNacin.adapter = adapter
            val nacinIndex = nacinOptions.indexOf(clan.nacinPlacanja).takeIf { it >= 0 } ?: 0
            spinnerNacin.setSelection(nacinIndex)

            etAdminDatumUplate.setText(clan.datumUplate)
            etAdminDatumIsteka.setText(clan.datumIsteka)

            btnAdminSpremi.setOnClickListener {
                val updated = clan.copy(
                    nacinPlacanja = spinnerNacin.selectedItem.toString(),
                    datumUplate = etAdminDatumUplate.text.toString().trim(),
                    datumIsteka = etAdminDatumIsteka.text.toString().trim()
                )
                db.clanDao().update(updated)
                Toast.makeText(this, "Izmjene spremljene", Toast.LENGTH_SHORT).show()
                osvjeziStatus()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
