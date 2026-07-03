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

@RunWith(AndroidJUnit4::class)
class EndToEndTest {

    @Before
    fun setUp() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        context.deleteDatabase("evidencija-db")
    }

    @Test
    fun registracijaLoginPregledClanova() {
        ActivityScenario.launch(MainActivity::class.java)

        // Klik na registraciju
        onView(withId(R.id.btn_registracija)).perform(click())

        // Ispuni registracijsku formu
        onView(withId(R.id.et_ime)).perform(replaceText("Ana"), closeSoftKeyboard())
        onView(withId(R.id.et_prezime)).perform(replaceText("Anic"), closeSoftKeyboard())
        onView(withId(R.id.et_email)).perform(replaceText("ana@test.com"), closeSoftKeyboard())
        onView(withId(R.id.et_lozinka)).perform(replaceText("ana123"), closeSoftKeyboard())

        // Klik na registriraj se
        onView(withId(R.id.btn_registracija)).perform(click())

        // Login s ispravnim podacima
        onView(withId(R.id.et_email)).perform(replaceText("ana@test.com"), closeSoftKeyboard())
        onView(withId(R.id.et_password)).perform(replaceText("ana123"), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())

        // Provjeri smo li na Home ekranu
        onView(withId(R.id.tv_ukupno)).check(matches(isDisplayed()))

        // Idi na listu članova
        onView(withId(R.id.btn_clanovi)).perform(click())

        // Provjeri je li Ana prikazana u listi
        onView(withText("Ana Anic")).check(matches(isDisplayed()))
    }
}