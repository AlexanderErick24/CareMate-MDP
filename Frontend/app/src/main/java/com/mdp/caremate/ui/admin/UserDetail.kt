//package com.mdp.caremate.ui.admin
//
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.google.firebase.firestore.FirebaseFirestore
//import com.mdp.caremate.R
//import com.mdp.caremate.data.model.User
//import com.mdp.caremate.databinding.FragmentUserDetailBinding
//
//class UserDetail : Fragment() {
//
//    private var _binding: FragmentUserDetailBinding? = null
//    private val binding get() = _binding!!
//
//    private var user: User? = null
//    private val db = FirebaseFirestore.getInstance()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // 1. Ambil data Parcelable secara aman
//        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            arguments?.getParcelable("ARG_USER", User::class.java)
//        } else {
//            @Suppress("DEPRECATION")
//            arguments?.getParcelable("ARG_USER")
//        }
//
//        // 2. Tampilkan data ke komponen UI jika data tidak null
//        user?.let { currentUser ->
//            displayUserData(currentUser)
//            setupStatusSwitch(currentUser)
//        }
//
//        // 3. Setup klik tombol kembali
//        binding.btnBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
//    }
//
//    private fun displayUserData(user: User) {
//        binding.tvDetailName.text = user.name
//        binding.tvDetailEmail.text = user.email
//        binding.tvDetailRole.text = user.role
//
//        if (user.role.equals("Caregiver", ignoreCase = true)) {
//            binding.tvDetailRole.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
//            binding.tvDetailRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//            binding.cardCaregiverCvSection.visibility = View.VISIBLE
//            binding.cardFamilySection.visibility = View.GONE
//
//            binding.tvJobTitle.text = "Job Title: ${user.jobTitle.ifEmpty { "-" }}"
//            binding.tvAge.text = "Umur: ${user.age} Tahun"
//            binding.tvBio.text = "Bio: ${user.bio.ifEmpty { "-" }}"
//
//            binding.tvExperienceList.text = if (user.experience.isNotEmpty()) {
//                user.experience.joinToString("\n") { "- $it" }
//            } else {
//                "-"
//            }
//            binding.tvSkillsList.text = if (user.skills.isNotEmpty()) {
//                user.skills.joinToString(", ")
//            } else {
//                "-"
//            }
//
//        } else {
//            // Perbaikan warna background/text agar kontras untuk Non-Caregiver (misal: warna biru/info)
//            binding.tvDetailRole.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light))
//            binding.tvDetailRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//            binding.cardCaregiverCvSection.visibility = View.GONE
//            binding.cardFamilySection.visibility = View.VISIBLE
//
//            binding.tvPairingCode.text = "Pairing Code: ${user.pairingCode.ifEmpty { "-" }}"
//            binding.tvPatientName.text = "Nama Pasien: ${user.patientName.ifEmpty { "-" }}"
//            binding.tvConnectedPatientUid.text = "Connected Patient UID: ${user.connectedPatientUid.ifEmpty { "-" }}"
//        }
//
//        // Set kondisi awal status switch tanpa memicu Listener database
//        binding.switchStatus.setOnCheckedChangeListener(null)
//        binding.switchStatus.isChecked = user.status
//    }
//
//    private fun setupStatusSwitch(user: User) {
//        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
//            // Proteksi: validasi jika UID kosong agar tidak salah tembak dokumen Firestore
//            if (user.uid.isEmpty()) {
//                Toast.makeText(requireContext(), "Gagal: UID User tidak ditemukan", Toast.LENGTH_SHORT).show()
//                binding.switchStatus.isChecked = !isChecked
//                return@setOnCheckedChangeListener
//            }
//
//            // Update status ke Firebase Firestore
//            db.collection("users").document(user.uid)
//                .update("status", isChecked)
//                .addOnSuccessListener {
//                    val message = if (isChecked) "Akun berhasil diaktifkan" else "Akun berhasil dinonaktifkan"
//                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { e ->
//                    // PERBAIKAN: Matikan listener sementara agar tidak terjadi loop saat posisi dibalikkan
//                    binding.switchStatus.setOnCheckedChangeListener(null)
//                    binding.switchStatus.isChecked = !isChecked
//
//                    // Pasang kembali listener setelah posisi switch berhasil dikembalikan
//                    setupStatusSwitch(user)
//
//                    Toast.makeText(requireContext(), "Gagal mengubah status: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

package com.mdp.caremate.ui.admin

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.R
import com.mdp.caremate.data.model.User
import com.mdp.caremate.databinding.FragmentUserDetailBinding

class UserDetail : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    private var user: User? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ambil data Parcelable secara aman
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("ARG_USER", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("ARG_USER")
        }

        // 2. Tampilkan data ke komponen UI jika data tidak null
        user?.let { currentUser ->
            displayUserData(currentUser)
            setupConnectionSection(currentUser) // Logika Tab/Section Relasi 1-ke-1 Baru
            setupStatusSwitch(currentUser)
        }

        // 3. Setup klik tombol kembali
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun displayUserData(user: User) {
        binding.tvDetailName.text = user.name
        binding.tvDetailEmail.text = user.email
        binding.tvDetailRole.text = user.role

        if (user.role.equals("Caregiver", ignoreCase = true)) {
            binding.tvDetailRole.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
            binding.tvDetailRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            binding.cardCaregiverCvSection.visibility = View.VISIBLE
            binding.cardFamilySection.visibility = View.GONE

            // Bind data CV Caregiver
            binding.tvJobTitle.text = "Job Title: ${user.jobTitle.ifEmpty { "-" }}"
            binding.tvAge.text = "Umur: ${user.age} Tahun"
            binding.tvBio.text = "Bio: ${user.bio.ifEmpty { "-" }}"

            binding.tvExperienceList.text = if (user.experience.isNotEmpty()) {
                user.experience.joinToString("\n") { "- $it" }
            } else {
                "-"
            }
            binding.tvSkillsList.text = if (user.skills.isNotEmpty()) {
                user.skills.joinToString(", ")
            } else {
                "-"
            }

            // PERBAIKAN LOGIKA: Pindahkan data asuhan pasien langsung ke panel Caregiver
            binding.tvPatientName.text = "Nama Pasien: ${user.patientName.ifEmpty { "-" }}"
            binding.tvConnectedPatientUid.text = "Connected Patient UID: ${user.connectedPatientUid.ifEmpty { "-" }}"

        } else {
            binding.tvDetailRole.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light))
            binding.tvDetailRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            binding.cardCaregiverCvSection.visibility = View.GONE
            binding.cardFamilySection.visibility = View.VISIBLE

            // Bind data Hubungan Keluarga (Hanya fokus menampilkan parameter pairingCode akun Family)
            binding.tvPairingCode.text = "Pairing Code: ${user.pairingCode.ifEmpty { "-" }}"
        }

        // Set kondisi awal status switch tanpa memicu Listener database
        binding.switchStatus.setOnCheckedChangeListener(null)
        binding.switchStatus.isChecked = user.status
    }

    /**
     * LOGIKA MAPPING RELASI SESUAI KETENTUAN PARAMETER
     */
    /**
     * LOGIKA MAPPING RELASI SESUAI KETENTUAN PARAMETER
     */
    private fun setupConnectionSection(currentUser: User) {
        val code = currentUser.pairingCode.trim()

        // Validasi awal jika pengguna belum terhubung dengan siapapun
        if (code.isEmpty() || code == "-") {
            binding.cardConnectionSection.visibility = View.VISIBLE
            binding.tvSharedPairingCode.text = "Status Hubungan: Mandiri"
            binding.tvTargetRelationInfo.text = "User ini belum terhubung ke relasi keluarga/caregiver manapun."
            binding.tvFamilyGroupList.text = "-"
            return
        }

        binding.cardConnectionSection.visibility = View.VISIBLE
        binding.tvSharedPairingCode.text = "Pairing Code Bersama: $code"

        // Jalankan pencarian grup berdasarkan pairingCode yang sama
        fetchFamilyGroupMembers(code, currentUser.uid)

        if (currentUser.role.equals("Caregiver", ignoreCase = true)) {
            // ================= CAREGIVER SIDE =================
            val caregiverUidParam = currentUser.caregiverUid.trim()
            val connectedPatientUidParam = currentUser.connectedPatientUid.trim()

            binding.tvTargetRelationInfo.text = "Menarik data relasi dari sistem..."

            if (caregiverUidParam.isNotEmpty()) {
                fetchUserFromFirestore(caregiverUidParam) { familyUser ->
                    if (connectedPatientUidParam.isNotEmpty()) {
                        fetchUserFromFirestore(connectedPatientUidParam) { patientUser ->
                            binding.tvTargetRelationInfo.text = """
                                👨‍👩‍👧‍👦 Terhubung ke Family:
                                • Nama: ${familyUser.name}
                                • Email: ${familyUser.email}
                                • UID: ${familyUser.uid}
                                
                                🧓 Terhubung ke Pasien:
                                • Nama: ${patientUser.name}
                                • UID Pasien: ${patientUser.uid}
                            """.trimIndent()
                        }
                    } else {
                        binding.tvTargetRelationInfo.text = """
                            👨‍👩‍👧‍👦 Terhubung ke Family:
                            • Nama: ${familyUser.name}
                            • Email: ${familyUser.email}
                            • UID: ${familyUser.uid}
                            
                            🧓 Terhubung ke Pasien:
                            • Nama Asli: ${currentUser.patientName.ifEmpty { "Belum terdata" }}
                            • UID Pasien: Kosong
                        """.trimIndent()
                    }
                }
            } else {
                binding.tvTargetRelationInfo.text = "Terikat pairingCode ($code) namun field data keluarga terhubung (caregiverUid) masih kosong."
            }

        } else {
            // ================= FAMILY SIDE =================
            val caregiverUidParam = currentUser.caregiverUid.trim()

            if (caregiverUidParam.isNotEmpty()) {
                binding.tvTargetRelationInfo.text = "Menghubungkan struktur Caregiver..."

                fetchUserFromFirestore(caregiverUidParam) { caregiverUser ->
                    val patientUidFromCaregiver = caregiverUser.connectedPatientUid.trim()

                    if (patientUidFromCaregiver.isNotEmpty()) {
                        fetchUserFromFirestore(patientUidFromCaregiver) { patientUser ->
                            binding.tvTargetRelationInfo.text = """
                                🩺 Caregiver Utama:
                                • Nama: ${caregiverUser.name}
                                • Email: ${caregiverUser.email}
                                • UID: ${caregiverUser.uid}
                                
                                🧓 Pasien yang Dirawat (via Caregiver):
                                • Nama: ${patientUser.name}
                                • UID Pasien: ${patientUser.uid}
                            """.trimIndent()
                        }
                    } else {
                        binding.tvTargetRelationInfo.text = """
                            🩺 Caregiver Utama:
                            • Nama: ${caregiverUser.name}
                            • Email: ${caregiverUser.email}
                            • UID: ${caregiverUser.uid}
                            
                            🧓 Pasien yang Dirawat:
                            • Data Pasien belum didaftarkan/dihubungkan oleh Caregiver terkait.
                        """.trimIndent()
                    }
                }
            } else {
                binding.tvTargetRelationInfo.text = "Terikat pairingCode ($code) tetapi belum ditautkan ke Caregiver (caregiverUid kosong)."
            }
        }
    }

    /**
     * FITUR BARU: Query ke Firestore mencari semua user dengan pairingCode yang sama
     */
    private fun fetchFamilyGroupMembers(pairingCode: String, currentUserUid: String) {
        binding.tvFamilyGroupList.text = "Mencari anggota grup..."

        db.collection("users")
            .whereEqualTo("pairingCode", pairingCode)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvFamilyGroupList.text = "-"
                    return@addOnSuccessListener
                }

                val memberLines = mutableListOf<String>()

                for (document in documents) {
                    val member = document.toObject(User::class.java)
                    // Beri penanda "(Anda)" jika itu akun yang sedang dibuka saat ini
                    val suffix = if (member.uid == currentUserUid) " (User Ini)" else ""

                    memberLines.add("- ${member.name} (${member.email}) [${member.role}]$suffix")
                }

                // Tampilkan semua baris anggota grup yang ditemukan
                binding.tvFamilyGroupList.text = memberLines.joinToString("\n")
            }
            .addOnFailureListener { e ->
                binding.tvFamilyGroupList.text = "Gagal memuat daftar anggota grup: ${e.message}"
            }
    }

    /**
     * Helper Network Fetcher dari Dokumen Koleksi `users`
     */
    private fun fetchUserFromFirestore(uid: String, onSuccess: (User) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val fetchedUser = document.toObject(User::class.java)
                if (fetchedUser != null) {
                    onSuccess(fetchedUser)
                } else {
                    binding.tvTargetRelationInfo.text = "Data UID: $uid tidak terdaftar di Firestore."
                }
            }
            .addOnFailureListener { e ->
                binding.tvTargetRelationInfo.text = "Gagal memproses relasi: ${e.message}"
            }
    }

    private fun setupStatusSwitch(user: User) {
        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            if (user.uid.isEmpty()) {
                Toast.makeText(requireContext(), "Gagal: UID User tidak ditemukan", Toast.LENGTH_SHORT).show()
                binding.switchStatus.isChecked = !isChecked
                return@setOnCheckedChangeListener
            }

            db.collection("users").document(user.uid)
                .update("status", isChecked)
                .addOnSuccessListener {
                    val message = if (isChecked) "Akun berhasil diaktifkan" else "Akun berhasil dinonaktifkan"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    binding.switchStatus.setOnCheckedChangeListener(null)
                    binding.switchStatus.isChecked = !isChecked
                    setupStatusSwitch(user)
                    Toast.makeText(requireContext(), "Gagal mengubah status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}