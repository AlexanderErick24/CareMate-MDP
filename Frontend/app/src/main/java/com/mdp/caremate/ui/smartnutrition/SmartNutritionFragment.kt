package com.mdp.caremate.ui.smartnutrition

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mdp.caremate.R
import com.mdp.caremate.data.model.PatientMedicalProfile
import com.mdp.caremate.databinding.FragmentSmartNutritionBinding

class SmartNutritionFragment : Fragment() {

    private var _binding: FragmentSmartNutritionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SmartNutritionViewModel by viewModels()
    private lateinit val adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmartNutritionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
        checkMedicalProfile()
    }

    private fun setupUI() {
        // Setup Spinner Waktu Makan
        val mealTypes = arrayOf("Sarapan", "Makan Siang", "Makan Malam", "Camilan")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mealTypes)
        binding.spinnerMealType.adapter = spinnerAdapter

        // Setup RecyclerView Grid 2 Kolom
        adapter = RecipeAdapter { recipe ->
            // Aksi saat Card ditekan -> Buka Halaman Detail Resep
            Toast.makeText(requireContext(), "Buka Detail: \${recipe.title}", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Recipe Detail Fragment
        }
        binding.rvRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecipes.adapter = adapter

        // Setup Tombol Generate
        binding.btnGenerate.setOnClickListener {
            val profile = getSavedMedicalProfile()
            if (profile != null) {
                val mealType = binding.spinnerMealType.selectedItem.toString()
                val ingredients = binding.etIngredients.text.toString().trim().takeIf { it.isNotEmpty() }
                
                viewModel.generateRecipes(profile, mealType, ingredients)
            } else {
                showMedicalProfileForm()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SmartNutritionState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvRecipes.visibility = View.GONE
                }
                is SmartNutritionState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvRecipes.visibility = View.GONE
                }
                is SmartNutritionState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvRecipes.visibility = View.VISIBLE
                    adapter.submitList(state.recipes)
                }
                is SmartNutritionState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkMedicalProfile() {
        val sharedPref = requireActivity().getSharedPreferences("CareMatePrefs", Context.MODE_PRIVATE)
        val isFilled = sharedPref.getBoolean("IS_MEDICAL_PROFILE_FILLED", false)
        if (!isFilled) {
            showMedicalProfileForm()
        }
    }

    private fun getSavedMedicalProfile(): PatientMedicalProfile? {
        val sharedPref = requireActivity().getSharedPreferences("CareMatePrefs", Context.MODE_PRIVATE)
        val isFilled = sharedPref.getBoolean("IS_MEDICAL_PROFILE_FILLED", false)
        if (!isFilled) return null
        
        return PatientMedicalProfile(
            diagnosis = sharedPref.getString("PATIENT_DIAGNOSIS", "") ?: "",
            allergies = sharedPref.getString("PATIENT_ALLERGIES", "") ?: "",
            texture = sharedPref.getString("PATIENT_TEXTURE", "") ?: "",
            preferences = sharedPref.getString("PATIENT_PREFERENCES", "") ?: ""
        )
    }

    private fun showMedicalProfileForm() {
        val bottomSheet = MedicalProfileBottomSheet()
        bottomSheet.onProfileSaved = { profile ->
            Toast.makeText(requireContext(), "Profil Medis Tersimpan", Toast.LENGTH_SHORT).show()
        }
        bottomSheet.show(parentFragmentManager, MedicalProfileBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
