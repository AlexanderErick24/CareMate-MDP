package com.mdp.caremate

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testLoginAndRegisterFlow() {
        // 1. Jalankan test pada Login Screen. Pastikan form login terlihat
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))

        // 2. Klik link register untuk berpindah ke halaman Register
        onView(withId(R.id.tvRegister)).perform(click())

        // 3. Verifikasi halaman Register terlihat
        onView(withId(R.id.etName)).check(matches(isDisplayed()))
        onView(withId(R.id.etPatientName)).check(matches(isDisplayed()))
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))

        // 4. Isi data pendaftaran (Register)
        onView(withId(R.id.etName)).perform(typeText("Erick"), closeSoftKeyboard())
        onView(withId(R.id.etPatientName)).perform(typeText("Opa Budi"), closeSoftKeyboard())
        onView(withId(R.id.etEmail)).perform(typeText("erick@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("password123"), closeSoftKeyboard())
        
        // Pilih role Family
        onView(withId(R.id.rbFamily)).perform(click())

        // 5. Kembali ke halaman login dengan mengklik tvLogin
        onView(withId(R.id.tvLogin)).perform(click())

        // 6. Masukkan data kredensial login
        onView(withId(R.id.etEmail)).perform(clearText(), typeText("erick@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(clearText(), typeText("password123"), closeSoftKeyboard())

        // 7. Klik login
        onView(withId(R.id.btnLogin)).perform(click())
    }
}
