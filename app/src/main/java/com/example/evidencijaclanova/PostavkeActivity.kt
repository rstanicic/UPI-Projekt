package com.example.evidencijaclanova

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class PostavkeActivity : AppCompatActivity() {

    private val logoOpcije = listOf("🏛️", "⚽", "🎵", "🤝", "🎭", "🌍")
    private var odabraniLogo: String = "🏛️"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postavke)

        val prefs = getSharedPreferences("postavke", MODE_PRIVATE)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val switchDarkMode = findViewById<SwitchCompat>(R.id.switch_dark_mode)

        val etNaziv = findViewById<TextInputEditText>(R.id.et_naziv_udruge)
        val etPrag = findViewById<TextInputEditText>(R.id.et_prag_upozorenja)
        val tvOdabraniLogo = findViewById<TextView>(R.id.tv_odabrani_logo)
        val btnSpremi = findViewById<MaterialButton>(R.id.btn_spremi_postavke)
        val btnOAplikaciji = findViewById<MaterialButton>(R.id.btn_o_aplikaciji)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open, R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Odjava na dnu drawera
        findViewById<android.widget.TextView>(R.id.tv_nav_odjava).setOnClickListener {
            drawerLayout.closeDrawers()
            Session.odjavi()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // --- Učitaj sačuvane vrijednosti ---
        etNaziv.setText(prefs.getString("naziv_udruge", "Moja udruga"))
        etPrag.setText(prefs.getInt("prag_upozorenja", 30).toString())
        odabraniLogo = prefs.getString("logo_udruge", "🏛️") ?: "🏛️"
        tvOdabraniLogo.text = "Odabrano: $odabraniLogo"
        osvjeziIzborLoga(odabraniLogo, tvOdabraniLogo)

        // --- Logo picker ---
        val logoIds = listOf(R.id.logo_1, R.id.logo_2, R.id.logo_3, R.id.logo_4, R.id.logo_5, R.id.logo_6)
        logoIds.forEachIndexed { i, viewId ->
            findViewById<TextView>(viewId).setOnClickListener {
                odabraniLogo = logoOpcije[i]
                tvOdabraniLogo.text = "Odabrano: $odabraniLogo"
                osvjeziIzborLoga(odabraniLogo, tvOdabraniLogo)
            }
        }

        // --- Spremi postavke udruge ---
        btnSpremi.setOnClickListener {
            val naziv = etNaziv.text?.toString()?.trim() ?: ""
            val pragTekst = etPrag.text?.toString()?.trim() ?: ""
            val prag = pragTekst.toIntOrNull() ?: 30

            if (naziv.isEmpty()) {
                etNaziv.error = "Unesite naziv"
                return@setOnClickListener
            }

            prefs.edit()
                .putString("naziv_udruge", naziv)
                .putString("logo_udruge", odabraniLogo)
                .putInt("prag_upozorenja", prag)
                .apply()

            Snackbar.make(btnSpremi, "✅ Postavke spremljene", Snackbar.LENGTH_SHORT).show()
        }

        // --- O aplikaciji ---
        btnOAplikaciji.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("$odabraniLogo O aplikaciji")
                .setMessage(
                    "ClubTrack\n\n" +
                    "Verzija: 1.0\n" +
                    "Kolegij: Uvod u programsko inženjerstvo 2025/2026\n\n" +
                    "Autori:\n" +
                    "• Roko Staničić\n" +
                    "• Dorian Bariša\n\n" +
                    "Aplikacija za upravljanje evidencijom članova udruge, plaćanjem članarina i pregledom statusa."
                )
                .setPositiveButton("Zatvori", null)
                .show()
        }

        // --- Dark mode ---
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            val intent = Intent(this, PostavkeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    Session.odjavi()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_clanovi -> startActivity(Intent(this, ClanoviActivity::class.java))
                R.id.nav_postavke -> { }
            }
            drawerLayout.closeDrawers()
            true
        }

        bottomNav.selectedItemId = R.id.nav_postavke
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_clanovi -> { startActivity(Intent(this, ClanoviActivity::class.java)); true }
                R.id.nav_postavke -> true
                else -> false
            }
        }
    }

    private fun osvjeziIzborLoga(odabrani: String, tvOdabrani: TextView) {
        val logoIds = listOf(R.id.logo_1, R.id.logo_2, R.id.logo_3, R.id.logo_4, R.id.logo_5, R.id.logo_6)
        logoIds.forEachIndexed { i, viewId ->
            val tv = findViewById<TextView>(viewId)
            if (logoOpcije[i] == odabrani) {
                tv.setBackgroundColor(Color.parseColor("#1565C0"))
                tv.alpha = 1.0f
            } else {
                tv.setBackgroundResource(R.drawable.pozadina_polja)
                tv.alpha = 0.6f
            }
        }
        tvOdabrani.text = "Odabrano: $odabrani"
    }
}
