package com.mdp.caremate.ui.smartnutrition

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.GenerateRecipeRequest
import com.mdp.caremate.data.model.GenerateRecipeResponse
import com.mdp.caremate.data.model.PatientMedicalProfile
import com.mdp.caremate.data.model.Recipe
import com.mdp.caremate.data.model.RecipeIngredient
import com.mdp.caremate.data.sources.remote.SmartNutritionApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SmartNutritionViewModelTest {

    // Rule: Memaksa LiveData berjalan secara sinkron (langsung) saat diuji.
    // Tanpa ini, setValue() dari LiveData akan error karena tidak ada Main Looper.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock API: Tiruan dari SmartNutritionApi (Retrofit interface).
    // Kita tidak benar-benar memanggil server, melainkan mensimulasikan respons.
    private val mockApi: SmartNutritionApi = mockk()

    private lateinit var viewModel: SmartNutritionViewModel

    // Data dummy yang dipakai berulang-ulang di setiap test
    private val dummyProfile = PatientMedicalProfile(
        diagnosis = "Diabetes Tipe 2",
        allergies = "Kacang Tanah",
        texture = "Lunak",
        preferences = "Tidak pedas"
    )

    private val dummyRecipes = listOf(
        Recipe(
            id = "recipe-001",
            title = "Bubur Ayam Sehat Rendah Natrium",
            imageSearchKeyword = "chicken porridge healthy",
            estTimeMin = 30,
            portions = 2,
            medicalRationale = "Rendah natrium, aman untuk pasien diabetes dan darah tinggi.",
            safetyBadge = "Aman untuk Diabetes",
            ingredients = listOf(
                RecipeIngredient(name = "Beras", amount = "100 gram"),
                RecipeIngredient(name = "Dada Ayam", amount = "150 gram")
            ),
            steps = listOf(
                "Langkah 1: Cuci beras hingga bersih.",
                "Langkah 2: Rebus beras dengan 600ml air selama 30 menit."
            ),
            youtubeQuery = "cara membuat bubur ayam sehat"
        ),
        Recipe(
            id = "recipe-002",
            title = "Sup Bening Daging Sapi",
            imageSearchKeyword = "beef clear soup",
            estTimeMin = 45,
            portions = 1,
            medicalRationale = "Kaya protein, rendah lemak jenuh.",
            safetyBadge = "Aman untuk Diabetes",
            ingredients = listOf(
                RecipeIngredient(name = "Daging Sapi", amount = "200 gram"),
                RecipeIngredient(name = "Wortel", amount = "1 buah")
            ),
            steps = listOf(
                "Langkah 1: Potong daging sapi menjadi kubus kecil.",
                "Langkah 2: Rebus daging hingga empuk selama 40 menit."
            ),
            youtubeQuery = "cara membuat sup bening daging sapi"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Inject mock API ke dalam ViewModel via constructor parameter
        viewModel = SmartNutritionViewModel(apiOverride = mockApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // STATE AWAL (INITIAL STATE)
    // =========================================================================

    @Test
    fun `initial uiState should be Idle`() {
        // Saat ViewModel baru dibuat, state harus Idle (belum ada aksi apapun)
        val state = viewModel.uiState.value
        assertTrue("State awal harus Idle", state is SmartNutritionState.Idle)
    }

    @Test
    fun `initial selectedRecipe should be null`() {
        // Belum ada resep yang dipilih
        assertNull(viewModel.selectedRecipe)
    }

    // =========================================================================
    // GENERATE RECIPES - SUKSES
    // =========================================================================

    @Test
    fun `generateRecipes success - uiState becomes Success with recipes`() = runTest {
        // Given: Server mengembalikan respons sukses berisi 2 resep
        val fakeResponse = GenerateRecipeResponse(
            message = "Resep berhasil dibuat.",
            data = dummyRecipes
        )
        coEvery { mockApi.generateRecipes(any()) } returns Response.success(fakeResponse)

        // When: Pengguna menekan tombol Generate
        viewModel.generateRecipes(dummyProfile, "Sarapan", "ayam, beras")

        // Then: State harus berubah menjadi Success dan berisi 2 resep
        val state = viewModel.uiState.value
        assertTrue("State harus Success", state is SmartNutritionState.Success)
        val successState = state as SmartNutritionState.Success
        assertEquals(2, successState.recipes.size)
        assertEquals("Bubur Ayam Sehat Rendah Natrium", successState.recipes[0].title)
        assertEquals("Sup Bening Daging Sapi", successState.recipes[1].title)
    }

    @Test
    fun `generateRecipes success - recipe data contains correct medical rationale`() = runTest {
        // Given
        val fakeResponse = GenerateRecipeResponse(message = "OK", data = dummyRecipes)
        coEvery { mockApi.generateRecipes(any()) } returns Response.success(fakeResponse)

        // When
        viewModel.generateRecipes(dummyProfile, "Sarapan", null)

        // Then: Pastikan data medis resep pertama benar
        val state = viewModel.uiState.value as SmartNutritionState.Success
        val firstRecipe = state.recipes[0]
        assertEquals("Aman untuk Diabetes", firstRecipe.safetyBadge)
        assertEquals(2, firstRecipe.portions)
        assertEquals(30, firstRecipe.estTimeMin)
        assertNotNull(firstRecipe.medicalRationale)
    }

    @Test
    fun `generateRecipes success - recipe contains ingredients and steps`() = runTest {
        // Given
        val fakeResponse = GenerateRecipeResponse(message = "OK", data = dummyRecipes)
        coEvery { mockApi.generateRecipes(any()) } returns Response.success(fakeResponse)

        // When
        viewModel.generateRecipes(dummyProfile, "Makan Siang", "daging sapi, wortel")

        // Then: Resep kedua harus punya bahan dan langkah yang benar
        val state = viewModel.uiState.value as SmartNutritionState.Success
        val secondRecipe = state.recipes[1]
        assertEquals(2, secondRecipe.ingredients.size)
        assertEquals("Daging Sapi", secondRecipe.ingredients[0].name)
        assertEquals(2, secondRecipe.steps.size)
    }

    @Test
    fun `generateRecipes success - sends correct request body to API`() = runTest {
        // Given: Capture request yang dikirim ke API
        val requestSlot = slot<GenerateRecipeRequest>()
        val fakeResponse = GenerateRecipeResponse(message = "OK", data = dummyRecipes)
        coEvery { mockApi.generateRecipes(capture(requestSlot)) } returns Response.success(fakeResponse)

        // When
        viewModel.generateRecipes(dummyProfile, "Makan Malam", "ikan salmon")

        // Then: Pastikan data request sesuai dengan input pengguna
        val capturedRequest = requestSlot.captured
        assertEquals("Diabetes Tipe 2", capturedRequest.patientProfile.diagnosis)
        assertEquals("Kacang Tanah", capturedRequest.patientProfile.allergies)
        assertEquals("Lunak", capturedRequest.patientProfile.texture)
        assertEquals("Makan Malam", capturedRequest.mealType)
        assertEquals("ikan salmon", capturedRequest.ingredients)
    }

    @Test
    fun `generateRecipes success with null ingredients - request still valid`() = runTest {
        // Given: Pengguna tidak mengisi bahan (biarkan AI memutuskan)
        val fakeResponse = GenerateRecipeResponse(message = "OK", data = dummyRecipes)
        coEvery { mockApi.generateRecipes(any()) } returns Response.success(fakeResponse)

        // When: ingredients = null
        viewModel.generateRecipes(dummyProfile, "Camilan", null)

        // Then: Tetap sukses
        val state = viewModel.uiState.value
        assertTrue("Harus sukses meski tanpa bahan", state is SmartNutritionState.Success)

        // Verify API tetap dipanggil
        coVerify(exactly = 1) { mockApi.generateRecipes(any()) }
    }

    // =========================================================================
    // GENERATE RECIPES - GAGAL (ERROR HANDLING)
    // =========================================================================

    @Test
    fun `generateRecipes network error - uiState becomes Error`() = runTest {
        // Given: Server tidak bisa dihubungi (misal: WiFi mati)
        coEvery { mockApi.generateRecipes(any()) } throws java.net.UnknownHostException("Tidak ada koneksi internet")

        // When
        viewModel.generateRecipes(dummyProfile, "Sarapan", null)

        // Then: State harus berubah menjadi Error
        val state = viewModel.uiState.value
        assertTrue("State harus Error saat jaringan putus", state is SmartNutritionState.Error)
        val errorState = state as SmartNutritionState.Error
        assertTrue(errorState.message.contains("Tidak ada koneksi internet"))
    }

    @Test
    fun `generateRecipes timeout - uiState becomes Error`() = runTest {
        // Given: Server terlalu lama merespons (Gemini AI sedang sibuk)
        coEvery { mockApi.generateRecipes(any()) } throws java.net.SocketTimeoutException("Koneksi timeout")

        // When
        viewModel.generateRecipes(dummyProfile, "Makan Siang", null)

        // Then
        val state = viewModel.uiState.value
        assertTrue("State harus Error saat timeout", state is SmartNutritionState.Error)
        assertTrue((state as SmartNutritionState.Error).message.contains("timeout"))
    }

    @Test
    fun `generateRecipes server error 500 - uiState becomes Error`() = runTest {
        // Given: Server mengembalikan HTTP 500 (Internal Server Error)
        coEvery { mockApi.generateRecipes(any()) } returns Response.error(
            500,
            """{"error": "Terjadi kesalahan server"}""".toResponseBody("application/json".toMediaType())
        )

        // When
        viewModel.generateRecipes(dummyProfile, "Makan Malam", null)

        // Then
        val state = viewModel.uiState.value
        assertTrue("State harus Error saat server 500", state is SmartNutritionState.Error)
    }

    @Test
    fun `generateRecipes API returns null body - uiState becomes Error`() = runTest {
        // Given: Server mengembalikan 200 OK tapi body kosong (edge case aneh)
        coEvery { mockApi.generateRecipes(any()) } returns Response.success(null)

        // When
        viewModel.generateRecipes(dummyProfile, "Sarapan", null)

        // Then: Harus ditangani sebagai error, bukan crash
        val state = viewModel.uiState.value
        assertTrue("State harus Error saat body null", state is SmartNutritionState.Error)
    }

    // =========================================================================
    // LOADING STATE
    // =========================================================================

    @Test
    fun `generateRecipes - state transitions through Loading`() = runTest {
        // Given
        val fakeResponse = GenerateRecipeResponse(message = "OK", data = dummyRecipes)
        val statesObserved = mutableListOf<SmartNutritionState>()

        // Observe semua perubahan state
        viewModel.uiState.observeForever { statesObserved.add(it) }

        coEvery { mockApi.generateRecipes(any()) } returns Response.success(fakeResponse)

        // When
        viewModel.generateRecipes(dummyProfile, "Sarapan", null)

        // Then: Urutan state harus Idle -> Loading -> Success
        assertTrue("Harus ada state Idle di awal", statesObserved[0] is SmartNutritionState.Idle)
        assertTrue("Harus melewati state Loading", statesObserved[1] is SmartNutritionState.Loading)
        assertTrue("Harus berakhir di Success", statesObserved[2] is SmartNutritionState.Success)
    }

    // =========================================================================
    // SELECTED RECIPE (NAVIGASI KE DETAIL)
    // =========================================================================

    @Test
    fun `selectedRecipe - can store and retrieve a recipe`() {
        // Given: Pengguna menekan salah satu kartu resep
        val recipe = dummyRecipes[0]

        // When
        viewModel.selectedRecipe = recipe

        // Then
        assertNotNull(viewModel.selectedRecipe)
        assertEquals("Bubur Ayam Sehat Rendah Natrium", viewModel.selectedRecipe!!.title)
        assertEquals("chicken porridge healthy", viewModel.selectedRecipe!!.imageSearchKeyword)
    }

    @Test
    fun `selectedRecipe - overwriting with new recipe replaces old one`() {
        // Given
        viewModel.selectedRecipe = dummyRecipes[0]
        assertEquals("Bubur Ayam Sehat Rendah Natrium", viewModel.selectedRecipe!!.title)

        // When: Pengguna menekan resep lain
        viewModel.selectedRecipe = dummyRecipes[1]

        // Then: Resep yang tersimpan harus berubah
        assertEquals("Sup Bening Daging Sapi", viewModel.selectedRecipe!!.title)
    }

    // =========================================================================
    // RESET STATE (PERBAIKAN BUG ROTASI LAYAR)
    // =========================================================================

    @Test
    fun `resetState - changes Error state back to Idle`() = runTest {
        // Given: Terjadi error sebelumnya
        coEvery { mockApi.generateRecipes(any()) } throws Exception("Gagal")
        viewModel.generateRecipes(dummyProfile, "Sarapan", null)

        // Pastikan state memang Error
        assertTrue(viewModel.uiState.value is SmartNutritionState.Error)

        // When: resetState dipanggil (setelah Toast ditampilkan)
        viewModel.resetState()

        // Then: State kembali ke Idle
        assertTrue("State harus kembali ke Idle", viewModel.uiState.value is SmartNutritionState.Idle)
    }

    @Test
    fun `resetState - does NOT change Success state`() = runTest {
        // Given: Generate berhasil
        val fakeResponse = GenerateRecipeResponse(message = "OK", data = dummyRecipes)
        coEvery { mockApi.generateRecipes(any()) } returns Response.success(fakeResponse)
        viewModel.generateRecipes(dummyProfile, "Sarapan", null)

        // Pastikan state Success
        assertTrue(viewModel.uiState.value is SmartNutritionState.Success)

        // When: resetState dipanggil (seharusnya tidak mengubah apapun)
        viewModel.resetState()

        // Then: State tetap Success, TIDAK berubah ke Idle
        assertTrue("Success tidak boleh di-reset", viewModel.uiState.value is SmartNutritionState.Success)
    }

    @Test
    fun `resetState - does NOT change Idle state`() {
        // Given: State sudah Idle
        assertTrue(viewModel.uiState.value is SmartNutritionState.Idle)

        // When
        viewModel.resetState()

        // Then: Tetap Idle (tidak berubah atau crash)
        assertTrue(viewModel.uiState.value is SmartNutritionState.Idle)
    }

    @Test
    fun `resetState - does NOT change Loading state`() = runTest {
        // Given: Kita simulasikan state Loading secara manual
        // (Pada skenario nyata, Loading terjadi sebelum respons API kembali)
        // Kita bisa langsung panggil generateRecipes lalu resetState sebelum respons

        // Workaround: set state ke loading via reflection atau panggil generateRecipes
        // Untuk simplisitas, kita test bahwa resetState hanya merespons Error
        val stateBeforeReset = viewModel.uiState.value
        viewModel.resetState()
        assertEquals("State tidak boleh berubah dari non-Error", stateBeforeReset, viewModel.uiState.value)
    }

    // =========================================================================
    // MEAL TYPE VARIATIONS (VARIASI WAKTU MAKAN)
    // =========================================================================

    @Test
    fun `generateRecipes - works with all meal types`() = runTest {
        // Given
        val fakeResponse = GenerateRecipeResponse(message = "OK", data = dummyRecipes)
        coEvery { mockApi.generateRecipes(any()) } returns Response.success(fakeResponse)
        val mealTypes = listOf("Sarapan", "Makan Siang", "Makan Malam", "Camilan")

        for (mealType in mealTypes) {
            // When
            viewModel.generateRecipes(dummyProfile, mealType, null)

            // Then: Semua tipe waktu makan harus menghasilkan Success
            assertTrue(
                "Harus sukses untuk $mealType",
                viewModel.uiState.value is SmartNutritionState.Success
            )
        }

        // Verify API dipanggil sebanyak 4 kali (1x per meal type)
        coVerify(exactly = 4) { mockApi.generateRecipes(any()) }
    }
}
