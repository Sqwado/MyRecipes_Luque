package com.example.myrecipes_luque.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.model.MealDetail
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.FragmentRecipeDetailBinding
import com.example.myrecipes_luque.databinding.ItemIngredientBinding
import com.example.myrecipes_luque.util.NutriScoreCalculator
import com.example.myrecipes_luque.util.ThumbLoader
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mealId = requireArguments().getString(ARG_MEAL_ID).orEmpty()
        binding.buttonFavorite.setOnClickListener {
            Snackbar.make(binding.root, R.string.favorite_coming_soon, Snackbar.LENGTH_SHORT).show()
        }
        loadDetail(mealId)
    }

    private fun loadDetail(mealId: String) {
        binding.progress.isVisible = true
        binding.textError.isVisible = false
        binding.contentScroll.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val meal = MealDbClient.api.getMealById(mealId).meals?.firstOrNull()
                if (meal == null) {
                    binding.textError.setText(R.string.error_loading_recipe_detail)
                    binding.textError.isVisible = true
                } else {
                    bindMeal(meal)
                    binding.contentScroll.isVisible = true
                }
            } catch (_: Exception) {
                binding.textError.setText(R.string.error_loading_recipe_detail)
                binding.textError.isVisible = true
            } finally {
                binding.progress.isVisible = false
            }
        }
    }

    private fun bindMeal(meal: MealDetail) {
        ThumbLoader.loadHero(binding.imageHero, meal.thumbUrl)

        val unavailable = getString(R.string.value_unavailable)
        binding.chipCategory.text = meal.category?.takeIf { it.isNotBlank() } ?: unavailable
        binding.chipArea.text = meal.area?.takeIf { it.isNotBlank() } ?: unavailable

        val nutriScore = NutriScoreCalculator.compute(meal)
        binding.nutriScoreView.setScorePercent(nutriScore)
        binding.textNutriScore.text = getString(R.string.nutri_score_value, nutriScore)

        binding.textInstructions.text =
            meal.instructions?.trim()?.takeIf { it.isNotEmpty() } ?: unavailable

        binding.containerIngredients.removeAllViews()
        val inflater = layoutInflater
        meal.ingredientLines().forEach { line ->
            val itemBinding = ItemIngredientBinding.inflate(
                inflater,
                binding.containerIngredients,
                false,
            )
            itemBinding.textIngredient.text = line.name
            itemBinding.textMeasure.text = line.measure.ifBlank { unavailable }
            binding.containerIngredients.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_MEAL_ID = "mealId"
        const val ARG_MEAL_NAME = "mealName"
    }
}
