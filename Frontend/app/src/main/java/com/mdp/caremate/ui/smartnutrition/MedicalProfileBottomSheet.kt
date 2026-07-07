package com.mdp.caremate.ui.smartnutrition

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mdp.caremate.data.model.PatientMedicalProfile
import com.mdp.caremate.databinding.BottomSheetMedicalProfileBinding

class MedicalProfileBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMedicalProfileBinding? = null
    private val binding get() = _binding!!

    // Callback when profile is saved
    var onProfileSaved: ((PatientMedicalProfile) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMedicalProfileBinding.inflate(inflater, container, false)
        // Mencegah bottom sheet ditutup dengan tap di luar jika ini pengisian pertama kali
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveProfile.setOnClickListener {
            val diagnosis = binding.etDiagnosis.text.toString().trim()
            val allergies = binding.etAllergies.text.toString().trim()
            val texture = binding.etTexture.text.toString().trim()
            val preferences = binding.etPreferences.text.toString().trim()

            if (diagnosis.isEmpty()) {
                Toast.makeText(context, "Diagnosis utama wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profile = PatientMedicalProfile(
                diagnosis = diagnosis,
                allergies = allergies.ifEmpty { "Tidak ada" },
                texture = texture.ifEmpty { "Normal" },
                preferences = preferences.ifEmpty { "Normal" }
            )

            // Simpan ke SharedPreferences
            val sharedPref = requireActivity().getSharedPreferences("CareMatePrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("PATIENT_DIAGNOSIS", profile.diagnosis)
                putString("PATIENT_ALLERGIES", profile.allergies)
                putString("PATIENT_TEXTURE", profile.texture)
                putString("PATIENT_PREFERENCES", profile.preferences)
                putBoolean("IS_MEDICAL_PROFILE_FILLED", true)
                apply()
            }

            onProfileSaved?.invoke(profile)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MedicalProfileBottomSheet"
    }
}
