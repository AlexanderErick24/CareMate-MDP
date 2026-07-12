package com.mdp.caremate.data.sources.remote

import com.mdp.caremate.data.model.AiAlert
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PremiumRemoteDataSourceVerifyMedicationTest {

    private val mockWebService: WebService = mockk(relaxed = true)

    // ====================================
    // TEST 1: Obat cocok → Severity SAFE
    // ====================================
    @Test
    fun verifyMedication_matchingMedication_returnsSafeAlert() = runTest {
        // GIVEN: Backend AI mengembalikan hasil SAFE (obat cocok)
        val expectedResponse = MedicationVerifyResponseJson(
            isValid = true,
            severity = "SAFE",
            title = "Medication Verified",
            description = "Obat di foto sesuai dengan Paracetamol 500mg.",
            timestamp = 1720000000000L
        )
        coEvery {
            mockWebService.verifyMedication(any())
        } returns expectedResponse

        val dataSource = PremiumRemoteDataSourceImpl(mockWebService)

        // WHEN: Caregiver mengirim foto obat Paracetamol
        val result: AiAlert = dataSource.verifyMedication(
            imageBase64 = "dummyBase64String",
            expectedMedication = "Paracetamol 500mg"
        )

        // THEN: Hasilnya harus SAFE dan propertinya benar
        assertEquals("SAFE", result.severity)
        assertEquals("Medication Verified", result.title)
        assertEquals("Obat di foto sesuai dengan Paracetamol 500mg.", result.description)
        assertEquals(1720000000000L, result.timestamp)
        assertNotNull(result.id) // ID harus ter-generate (UUID)
    }

    // ====================================
    // TEST 2: Obat TIDAK cocok → Severity HIGH
    // ====================================
    @Test
    fun verifyMedication_mismatchMedication_returnsHighAlert() = runTest {
        // GIVEN: Backend AI mengembalikan hasil HIGH (obat salah)
        val expectedResponse = MedicationVerifyResponseJson(
            isValid = false,
            severity = "HIGH",
            title = "Wrong Medication Detected",
            description = "Foto menunjukkan Amoxicillin, bukan Paracetamol 500mg.",
            timestamp = 1720000000000L
        )
        coEvery {
            mockWebService.verifyMedication(any())
        } returns expectedResponse

        val dataSource = PremiumRemoteDataSourceImpl(mockWebService)

        // WHEN
        val result: AiAlert = dataSource.verifyMedication(
            imageBase64 = "dummyBase64WrongMed",
            expectedMedication = "Paracetamol 500mg"
        )

        // THEN: Severity harus HIGH
        assertEquals("HIGH", result.severity)
        assertEquals("Wrong Medication Detected", result.title)
    }

    // ====================================
    // TEST 3: imageBase64 tersimpan di imageUrl
    // ====================================
    @Test
    fun verifyMedication_preservesBase64InImageUrl() = runTest {
        // GIVEN
        val base64Input = "iVBORw0KGgoAAAANSUhEUgAA..."
        coEvery {
            mockWebService.verifyMedication(any())
        } returns MedicationVerifyResponseJson(
            isValid = true,
            severity = "SAFE",
            title = "OK",
            description = "OK",
            timestamp = 123L
        )

        val dataSource = PremiumRemoteDataSourceImpl(mockWebService)

        // WHEN
        val result = dataSource.verifyMedication(base64Input, "Vitamin C")

        // THEN: imageUrl harus menyimpan base64 asli dari input
        assertEquals(base64Input, result.imageUrl)
    }

    // ====================================
    // TEST 4: UUID selalu unik di tiap panggilan
    // ====================================
    @Test
    fun verifyMedication_generatesUniqueId() = runTest {
        coEvery {
            mockWebService.verifyMedication(any())
        } returns MedicationVerifyResponseJson(
            isValid = true,
            severity = "SAFE",
            title = "OK",
            description = "OK",
            timestamp = 123L
        )

        val dataSource = PremiumRemoteDataSourceImpl(mockWebService)

        val result1 = dataSource.verifyMedication("img1", "Med A")
        val result2 = dataSource.verifyMedication("img2", "Med B")

        // Dua panggilan berbeda harus menghasilkan ID berbeda
        assertNotNull(result1.id)
        assertNotNull(result2.id)
        assert(result1.id != result2.id) { "Setiap AiAlert harus memiliki UUID unik" }
    }
}
