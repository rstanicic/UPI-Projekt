package com.example.evidencijaclanova

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ClanAdapter(
    private val clanovi: MutableList<Clan>,
    private val onStatusChanged: (Clan) -> Unit = {}
) : RecyclerView.Adapter<ClanAdapter.ClanViewHolder>() {

    class ClanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.card_clan)
        val avatar: TextView = view.findViewById(R.id.tv_avatar)
        val ime: TextView = view.findViewById(R.id.tv_ime)
        val aktivan: CheckBox = view.findViewById(R.id.cb_aktivan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clan, parent, false)
        return ClanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClanViewHolder, position: Int) {
        val clan = clanovi[position]

        holder.ime.text = "${clan.ime} ${clan.prezime}"

        // Avatar po spolu
        holder.avatar.text = when (clan.spol) {
            "M" -> "👨"
            "Ž" -> "👩"
            else -> "👤"
        }

        // Boja kartice
        holder.card.setCardBackgroundColor(
            if (clan.aktivan) Color.parseColor("#E8F5E9")
            else Color.parseColor("#FFEBEE")
        )

        // Checkbox samo prikazuje status, ne mijenja ga
        holder.aktivan.setOnCheckedChangeListener(null)
        holder.aktivan.isChecked = clan.aktivan
        holder.aktivan.text = if (clan.aktivan) "Aktivan" else "Neaktivan"
        holder.aktivan.isClickable = false
        holder.aktivan.isFocusable = false

        // Klik na cijelu karticu otvara detalje
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetaljiClanaActivity::class.java)
            intent.putExtra("clan_id", clan.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = clanovi.size
}