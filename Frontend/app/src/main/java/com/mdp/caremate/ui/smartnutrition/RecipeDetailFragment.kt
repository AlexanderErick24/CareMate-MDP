package com.mdp.caremate.ui.smartnutrition

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.mdp.caremate.data.model.Recipe
import com.mdp.caremate.databinding.FragmentRecipeDetailBinding

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel if we want to pass data or trigger another generate request
    private val viewModel: SmartNutritionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Asumsi data resep dikirim melalui Navigation Component (SafeArgs) atau bundle statis.
        // Di aplikasi nyata, kita gunakan argumen dari Navigation graph.
        
        // Dummy mock data for UI binding demonstration
        setupUI()
    }

    private fun setupUI() {
        // Contoh binding, di produksi ini diambil dari argumen (SafeArgs)
        binding.tvRecipeTitle.text = "Sup Ayam Bayam Sehat"
        binding.tvMedicalRationale.text = "Sup ayam bayam sangat baik untuk pasien Hipertensi karena kaldu dapat dibuat rendah natrium, dan bayam kaya akan kalium yang membantu mengontrol tekanan darah."
        
        val ingredientsText = "- 100gr Dada Ayam\n- 2 Ikat Bayam\n- 1 Siung Bawang Putih"
        binding.tvIngredients.text = ingredientsText
        
        val stepsText = "1. Rebus dada ayam hingga empuk.\n2. Masukkan bawang putih cincang.\n3. Tambahkan bayam dan matikan api agar nutrisi terjaga."
        binding.tvSteps.text = stepsText

        val imageUrl = "https://source.unsplash.com/800x600/?chicken,soup"
        binding.ivRecipeImage.load(imageUrl) {
            crossfade(true)
        }

        binding.btnYoutube.setOnClickListener {
            val query = "cara membuat sup ayam bayam sehat"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=\$query"))
            startActivity(intent)
        }

        // Logika Chat Revisi
        binding.btnSendRevision.setOnClickListener {
            val revision = binding.etRevisionChat.text.toString().trim()
            if (revision.isNotEmpty()) {
                Toast.makeText(requireContext(), "Merevisi resep...", Toast.LENGTH_SHORT).show()
                // Di sini panggil fungsi viewModel untuk merevisi:
                // viewModel.generateRecipes(..., revisionPrompt = revision)
                
                binding.etRevisionChat.text.clear()
                
                // Kembali ke fragment sebelumnya (Grid View) atau tampilkan loading overlay
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
