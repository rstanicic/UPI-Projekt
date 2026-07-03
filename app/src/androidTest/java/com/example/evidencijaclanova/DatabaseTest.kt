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
class DatabaseTest {

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
    fun writeAndReadClan() {
        val clan = Clan(
            ime = "Test",
            prezime = "Testić",
            email = "test@test.com",
            lozinka = "1234"
        )
        clanDao.insert(clan)

        val svi = clanDao.getAll()
        assertEquals(1, svi.size)
        assertEquals("Test", svi[0].ime)
        assertEquals("Testić", svi[0].prezime)
        assertEquals("test@test.com", svi[0].email)
    }
}