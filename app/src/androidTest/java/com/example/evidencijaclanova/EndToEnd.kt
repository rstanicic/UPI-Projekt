package com.example.evidencijaclanova

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end test koji simulira prijavu administratora u aplikaciju ClubTrack,
 * provjeru početnog ekrana i navigaciju na popis članova.
 * Koristi Espresso UI testing framework.
 */
@RunWith(AndroidJUnit4::class)
class EndToEndTest {

    @Before
    fun setUp() {
        // Resetiraj sesiju da bi login ekran bio prikazan
        Session.isAdmin = false
        Session.currentClan = null
    }

    @Test
    fun adminLoginINavigacijaNaClanove() {
        // Pokreni login ekran
        ActivityScenario.launch(MainActivity::class.java)

        // Unesi admin kredencijale
        onView(withId(R.id.et_email))
            .perform(replaceText("admin@admin.com"), closeSoftKeyboard())
        onView(withId(R.id.et_password))
            .perform(replaceText("admin123"), closeSoftKeyboard())

        // Klikni "Prijavi se"
        onView(withId(R.id.btn_login)).perform(click())

        // Provjeri da je HomeActivity otvoren (statistika vidljiva)
        onView(withId(R.id.tv_ukupno)).check(matches(isDisplayed()))

        // Navigiraj na popis članova
        onView(withId(R.id.btn_clanovi)).perform(click())

        // Provjeri da je RecyclerView s članovima vidljiv
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }
}
