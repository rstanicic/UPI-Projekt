package com.example.evidencijaclanova

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import android.widget.TextView

class HomeActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        db = AppDatabase.getDatabase(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val btnClanovi = findViewById<MaterialButton>(R.id.btn_clanovi)

        setSupportActionBar(toolbar)

        // Prikaz uloge u toolbaru
        if (Session.isAdmin) {
            supportActionBar?.subtitle = "⚙️ Administrator"
        } else {
            val ime = Session.currentClan?.ime ?: ""
            if (ime.isNotEmpty()) supportActionBar?.subtitle = "👤 $ime"
        }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open, R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

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
                R.id.nav_home -> { }
                R.id.nav_clanovi -> startActivity(Intent(this, ClanoviActivity::class.java))
                R.id.nav_postavke -> startActivity(Intent(this, PostavkeActivity::class.java))
                R.id.nav_logout -> {
                    Session.odjavi()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_clanovi -> { startActivity(Intent(this, ClanoviActivity::class.java)); true }
                R.id.nav_postavke -> { startActivity(Intent(this, PostavkeActivity::class.java)); true }
                else -> false
            }
        }

        btnClanovi.setOnClickListener {
            startActivity(Intent(this, ClanoviActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        osvjeziStatistiku()
    }

    private fun osvjeziStatistiku() {
        val tvUkupno = findViewById<TextView>(R.id.tv_ukupno)
        val tvAktivni = findViewById<TextView>(R.id.tv_aktivni)

        val sviClanovi = db.clanDao().getAll()
        val ukupno = sviClanovi.size
        val aktivnih = sviClanovi.count { it.aktivan }
        val neaktivnih = ukupno - aktivnih
        val platili = sviClanovi.count { it.platioClanarinu }

        tvUkupno.text = "👥 Ukupno članova: $ukupno"
        tvAktivni.text = "✅ Aktivnih: $aktivnih  |  ❌ Neaktivnih: $neaktivnih  |  💰 Platili: $platili"
    }
}
