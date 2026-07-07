package com.mdp.caremate.ui.smartnutrition

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mdp.caremate.data.model.Recipe
import com.mdp.caremate.databinding.ItemRecipeCardBinding

class RecipeAdapter(private val onItemClick: (Recipe) -> Unit) :
    ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.tvRecipeTitle.text = recipe.title
            binding.tvSafetyBadge.text = recipe.safetyBadge
            binding.tvEstTime.text = "\${recipe.estTimeMin} Menit"
            
            // Menggunakan Coil untuk mencari gambar stok dari Unsplash berdasarkan keyword dari Gemini
            val imageUrl = "https://source.unsplash.com/400x300/?\${recipe.imageSearchKeyword.replace(" ", ",")},food"
            binding.ivRecipeImage.load(imageUrl) {
                crossfade(true)
                // Di aplikasi nyata, tambahkan placeholder dan error image
            }

            binding.root.setOnClickListener {
                onItemClick(recipe)
            }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
    }
}
