package com.example.evidencijaclanova

object Session {
    var isAdmin: Boolean = false
    var currentClan: Clan? = null

    fun odjavi() {
        isAdmin = false
        currentClan = null
    }
}
