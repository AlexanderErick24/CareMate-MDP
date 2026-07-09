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
        val recipe = viewModel.selectedRecipe
        if (recipe != null) {
            binding.tvRecipeTitle.text = recipe.title
            binding.tvMedicalRationale.text = recipe.medicalRationale
            
            val portions = recipe.portions ?: 1
            val portionsText = "Porsi: $portions Orang\n\n"
            val ingredientsText = portionsText + recipe.ingredients.joinToString("\n") { "- ${it.name} ${it.amount}" }
            binding.tvIngredients.text = ingredientsText
            
            val stepsText = recipe.steps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")
            binding.tvSteps.text = stepsText

            val keyword = java.net.URLEncoder.encode(recipe.imageSearchKeyword + " food", "UTF-8")
            val imageUrl = "https://image.pollinations.ai/prompt/${keyword}?width=600&height=400&nologo=true"
            binding.ivRecipeImage.load(imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }

            binding.btnYoutube.setOnClickListener {
                val query = recipe.youtubeQuery ?: "cara membuat ${recipe.title}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query"))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Tidak ada aplikasi browser/youtube untuk membuka link", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
