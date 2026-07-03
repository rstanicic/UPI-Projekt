package com.example.evidencijaclanova

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistracijaLoginTest {

    private lateinit var db: AppDatabase
    private lateinit var clanDao: ClanDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        clanDao = db.clanDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun registracijaILoginTest() {
        // Registracija novog člana
        val clan = Clan(
            ime = "Filip",
            prezime = "Filipić",
            email = "filip@test.com",
            lozinka = "filip123"
        )
        clanDao.insert(clan)

        // Provjera je li član upisan u bazu
        val svi = clanDao.getAll()
        assertEquals(1, svi.size)

        // Login s ispravnim podacima
        val loginClan = clanDao.login("filip@test.com", "filip123")
        assertNotNull(loginClan)
        assertEquals("Filip", loginClan?.ime)

        // Login s pogrešnim podacima
        val pogresanLogin = clanDao.login("filip@test.com", "kriva_lozinka")
        assertNull(pogresanLogin)
    }
}