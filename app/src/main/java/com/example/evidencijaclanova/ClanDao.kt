package com.example.evidencijaclanova

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ClanDao {

    @Insert
    fun insert(clan: Clan)

    @Query("SELECT * FROM clanovi")
    fun getAll(): List<Clan>

    @Query("SELECT * FROM clanovi WHERE id = :id")
    fun getById(id: Int): Clan?

    @Query("SELECT * FROM clanovi WHERE email = :email AND lozinka = :lozinka LIMIT 1")
    fun login(email: String, lozinka: String): Clan?

    @Update
    fun update(clan: Clan)

    @Query("DELETE FROM clanovi WHERE id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM clanovi WHERE platioClanarinu = 0")
    fun getNijePlatili(): List<Clan>
}