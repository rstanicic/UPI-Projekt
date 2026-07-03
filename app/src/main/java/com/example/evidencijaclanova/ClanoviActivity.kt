package com.example.evidencijaclanova

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class ClanoviActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ClanAdapter
    private lateinit var btnSvi: MaterialButton
    private lateinit var btnAktivni: MaterialButton
    private lateinit var btnNeaktivni: MaterialButton
    private val sviClanovi = mutableListOf<Clan>()
    private val prikazaniClanovi = mutableListOf<Clan>()
    private var trenutniFilter = "svi"
    private var trenutnaPretraga = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clanovi)

        db = AppDatabase.getDatabase(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val etPretraga = findViewById<TextInputEditText>(R.id.et_pretraga)
        val btnDodaj = findViewById<MaterialButton>(R.id.btn_dodaj)
        btnSvi = findViewById(R.id.btn_svi)
        btnAktivni = findViewById(R.id.btn_aktivni)
        btnNeaktivni = findViewById(R.id.btn_neaktivni)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open, R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        sviClanovi.addAll(db.clanDao().getAll())
        prikazaniClanovi.addAll(sviClanovi)

        adapter = ClanAdapter(prikazaniClanovi) { updatedClan ->
            db.clanDao().update(updatedClan)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        postaviAktivniGumb(btnSvi)

        etPretraga.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                trenutnaPretraga = s.toString()
                primijeniFilter()
            }
        })

        btnDodaj.setOnClickListener {
            val intent = Intent(this, RegistracijaClanova::class.java)
            intent.putExtra("iz_clanovi", true)
            startActivity(intent)
        }

        btnSvi.setOnClickListener {
            trenutniFilter = "svi"
            postaviAktivniGumb(btnSvi)
            primijeniFilter()
        }
        btnAktivni.setOnClickListener {
            trenutniFilter = "aktivni"
            postaviAktivniGumb(btnAktivni)
            primijeniFilter()
        }
        btnNeaktivni.setOnClickListener {
            trenutniFilter = "neaktivni"
            postaviAktivniGumb(btnNeaktivni)
            primijeniFilter()
        }

        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val obrisaniClan = prikazaniClanovi[position]

                db.clanDao().delete(obrisaniClan.id)
                sviClanovi.remove(obrisaniClan)
                prikazaniClanovi.removeAt(position)
                adapter.notifyItemRemoved(position)

                Snackbar.make(recyclerView, "Član ${obrisaniClan.ime} obrisan", Snackbar.LENGTH_LONG)
                    .setAction("PONIŠTI") {
                        db.clanDao().insert(obrisaniClan)
                        sviClanovi.add(obrisaniClan)
                        primijeniFilter()
                    }.show()
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val paint = Paint()
                paint.color = Color.RED
                val itemView = viewHolder.itemView
                c.drawRect(
                    itemView.right + dX, itemView.top.toFloat(),
                    itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                )
                val textPaint = Paint()
                textPaint.color = Color.WHITE
                textPaint.textSize = 40f
                c.drawText("🗑 Obriši", itemView.right - 220f,
                    itemView.top + (itemView.height / 2f) + 15f, textPaint)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
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
                R.id.nav_clanovi -> { }
                R.id.nav_postavke -> startActivity(Intent(this, PostavkeActivity::class.java))
                R.id.nav_logout -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        bottomNav.selectedItemId = R.id.nav_clanovi
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_clanovi -> true
                R.id.nav_postavke -> { startActivity(Intent(this, PostavkeActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun postaviAktivniGumb(aktivni: MaterialButton) {
        listOf(btnSvi, btnAktivni, btnNeaktivni).forEach { btn ->
            btn.setBackgroundColor(Color.TRANSPARENT)
            btn.setTextColor(Color.parseColor("#1565C0"))
        }
        aktivni.setBackgroundColor(Color.parseColor("#1565C0"))
        aktivni.setTextColor(Color.WHITE)
    }

    private fun primijeniFilter() {
        val filtrirani = sviClanovi.filter { clan ->
            val odgovaraPretrazi = "${clan.ime} ${clan.prezime}"
                .contains(trenutnaPretraga, ignoreCase = true)
            val odgovaraFilteru = when (trenutniFilter) {
                "aktivni" -> clan.aktivan
                "neaktivni" -> !clan.aktivan
                else -> true
            }
            odgovaraPretrazi && odgovaraFilteru
        }
        prikazaniClanovi.clear()
        prikazaniClanovi.addAll(filtrirani)
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        sviClanovi.clear()
        sviClanovi.addAll(db.clanDao().getAll())
        primijeniFilter()
    }
}