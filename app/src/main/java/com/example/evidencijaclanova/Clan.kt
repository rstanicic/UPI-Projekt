package com.example.evidencijaclanova

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clanovi")
data class Clan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ime: String,
    val prezime: String,
    val email: String,
    val lozinka: String,
    val oib: String = "",
    val telefon: String = "",
    val datumRodjenja: String = "",
    val spol: String = "",
    val opis: String = "",
    var aktivan: Boolean = true,
    var platioClanarinu: Boolean = false
)