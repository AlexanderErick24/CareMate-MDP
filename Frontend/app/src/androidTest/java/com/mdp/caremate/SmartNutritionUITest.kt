package com.mdp.caremate

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmartNutritionUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSmartNutritionFormSubmission() {
        // Simulasikan pendaftaran profil medis jika BottomSheet muncul
        // Karena profil medis bisa jadi kosong saat pertama kali dibuka,
        // Espresso akan mengecek apakah EditText bottom sheet ditampilkan.
        try {
            onView(withId(R.id.etDiagnosis)).perform(typeText("Diabetes"), closeSoftKeyboard())
            onView(withId(R.id.etAllergies)).perform(typeText("Kacang"), closeSoftKeyboard())
            onView(withId(R.id.etTexture)).perform(typeText("Lunak"), closeSoftKeyboard())
            onView(withId(R.id.etPreferences)).perform(typeText("Halal"), closeSoftKeyboard())
            onView(withId(R.id.btnSaveProfile)).perform(click())
        } catch (e: Exception) {
            // Profile sudah pernah terisi sebelumnya, bottom sheet tidak muncul otomatis
        }

        // Isi form utama Smart Nutrition
        onView(withId(R.id.etIngredients)).perform(clearText(), typeText("Ayam, Wortel"), closeSoftKeyboard())

        // Klik tombol generate resep
        onView(withId(R.id.btnGenerate)).perform(click())

        // Pastikan loading state atau recyclerview resep termuat
        // (Espresso akan mengecek keberadaan view RV atau progress bar)
        onView(withId(R.id.progressBar)).check(matches(isDisplayed()))
    }
}
