package com.example.evidencijaclanova

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class ClanAdapter(
    private val clanovi: MutableList<Clan>,
    private val onStatusChanged: (Clan) -> Unit = {}
) : RecyclerView.Adapter<ClanAdapter.ClanViewHolder>() {

    class ClanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.card_clan)
        val avatar: TextView = view.findViewById(R.id.tv_avatar)
        val ime: TextView = view.findViewById(R.id.tv_ime)
        val statusClanarine: TextView = view.findViewById(R.id.tv_status_clanarine)
        val aktivan: CheckBox = view.findViewById(R.id.cb_aktivan)
        val clanarinaBtn: TextView = view.findViewById(R.id.tv_clanarina_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clan, parent, false)
        return ClanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClanViewHolder, position: Int) {
        val clan = clanovi[position]
        val context = holder.itemView.context

        holder.ime.text = "${clan.ime} ${clan.prezime}"

        // Avatar po spolu
        holder.avatar.text = when (clan.spol) {
            "M" -> "👨"
            "Ž" -> "👩"
            else -> "👤"
        }

        // Status članarine
        val (statusTekst, statusBoja) = izracunajStatus(clan)
        holder.statusClanarine.text = statusTekst
        holder.statusClanarine.setTextColor(statusBoja)

        // Boja kartice prema aktivnosti (dark mode aware)
        holder.card.setCardBackgroundColor(
            if (clan.aktivan) ContextCompat.getColor(context, R.color.active_bg)
            else ContextCompat.getColor(context, R.color.inactive_bg)
        )

        // Checkbox — samo admin može mijenjati aktivnost direktno u listi
        holder.aktivan.setOnCheckedChangeListener(null)
        holder.aktivan.isChecked = clan.aktivan
        holder.aktivan.text = if (clan.aktivan) "Aktivan" else "Neaktivan"

        if (Session.isAdmin) {
            holder.aktivan.isClickable = true
            holder.aktivan.isFocusable = true
            holder.aktivan.setOnCheckedChangeListener { _, isChecked ->
                clan.aktivan = isChecked
                holder.aktivan.text = if (isChecked) "Aktivan" else "Neaktivan"
                holder.card.setCardBackgroundColor(
                    if (isChecked) ContextCompat.getColor(context, R.color.active_bg)
                    else ContextCompat.getColor(context, R.color.inactive_bg)
                )
                onStatusChanged(clan)
            }
        } else {
            holder.aktivan.isClickable = false
            holder.aktivan.isFocusable = false
            holder.aktivan.setOnCheckedChangeListener(null)
        }

        // Klik na karticu otvara detalje
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetaljiClanaActivity::class.java)
            intent.putExtra("clan_id", clan.id)
            context.startActivity(intent)
        }

        // Klik na "💳 Članarina" otvara ClanarinaActivity
        holder.clanarinaBtn.setOnClickListener {
            val intent = Intent(context, ClanarinaActivity::class.java)
            intent.putExtra("clan_id", clan.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = clanovi.size

    companion object {
        fun izracunajStatus(clan: Clan): Pair<String, Int> {
            if (!clan.platioClanarinu) return Pair("💔 Nije platio", 0xFFE53935.toInt())

            if (clan.datumIsteka.isNotEmpty()) {
                return try {
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val istekDatum = sdf.parse(clan.datumIsteka)
                    val danas = Date()
                    val razlikaMs = istekDatum!!.time - danas.time
                    val razlikaDana = razlikaMs / (1000 * 60 * 60 * 24)
                    when {
                        razlikaDana < 0 -> Pair("❌ Isteklo", 0xFFE53935.toInt())
                        razlikaDana < 30 -> Pair("⚠️ Ističe za ${razlikaDana}d", 0xFFFF8F00.toInt())
                        else -> Pair("✅ Plaćeno do ${clan.datumIsteka}", 0xFF43A047.toInt())
                    }
                } catch (e: Exception) {
                    Pair("✅ Plaćeno", 0xFF43A047.toInt())
                }
            }

            return Pair("✅ Plaćeno", 0xFF43A047.toInt())
        }
    }
}
