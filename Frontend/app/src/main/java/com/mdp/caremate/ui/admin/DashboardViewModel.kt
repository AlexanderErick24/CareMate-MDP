package com.mdp.caremate.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _activeUsersCount = MutableLiveData<Int>()
    val activeUsersCount: LiveData<Int> get() = _activeUsersCount

    private val _activeEventsCount = MutableLiveData<Int>()
    val activeEventsCount: LiveData<Int> get() = _activeEventsCount

    private val _totalRevenue = MutableLiveData<Long>()
    val totalRevenue: LiveData<Long> get() = _totalRevenue

    private val _premiumUsersCount = MutableLiveData<Int>()
    val premiumUsersCount: LiveData<Int> get() = _premiumUsersCount

    fun fetchDashboardStats() {
        viewModelScope.launch {
            // 1. Ambil user & hitung totalnya
            val users = getAllUser()
            _activeUsersCount.value = users.size

            // 2. Hitung jumlah user premium & total revenue (Rp 50.000 per user)
            val premiumCount = users.count { it.isPremium }
            _premiumUsersCount.value = premiumCount
            _totalRevenue.value = premiumCount * 50_000L

            // 3. Ambil event & hitung yang listed == true DAN belum lewat dari sekarang
            val events = getAllEvent()

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val now = Calendar.getInstance().time

            val activeAndUpcomingCount = events.count { event ->
                // Pastikan status listed bernilai true
                if (!event.listed) return@count false

                try {
                    // Gabungkan date dan time (contoh: "25/12/2026" + " " + "19:00")
                    val fullDateTimeStr = "${event.date} ${event.time}"
                    val eventDateTime = sdf.parse(fullDateTimeStr)

                    // Event dianggap aktif jika tanggalnya setelah waktu saat ini (now)
                    eventDateTime != null && eventDateTime.after(now)
                } catch (e: Exception) {
                    // Jika format tanggal salah/gagal parse, jangan dihitung sebagai event aktif
                    false
                }
            }

            _activeEventsCount.value = activeAndUpcomingCount
        }
    }

    private suspend fun getAllEvent(): List<Event> {
        return try {
            firestore.collection("events").get().await().toObjects(Event::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getAllUser(): List<User> {
        return try {
            firestore.collection("users").get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}