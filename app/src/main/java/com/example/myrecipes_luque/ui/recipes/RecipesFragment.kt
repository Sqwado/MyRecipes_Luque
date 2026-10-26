package com.example.myrecipes_luque.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.FragmentRecipesBinding
import com.example.myrecipes_luque.ui.detail.RecipeDetailFragment
import kotlinx.coroutines.launch

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private val adapter = RecipesAdapter { meal ->
        val args = Bundle().apply {
            putString(RecipeDetailFragment.ARG_MEAL_ID, meal.id)
            putString(RecipeDetailFragment.ARG_MEAL_NAME, meal.name)
        }
        findNavController().navigate(
            R.id.action_recipesFragment_to_recipeDetailFragment,
            args,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categoryName = requireArguments().getString(ARG_CATEGORY_NAME).orEmpty()

        binding.recyclerRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecipes.adapter = adapter
        loadRecipes(categoryName)
    }

    private fun loadRecipes(categoryName: String) {
        binding.progress.isVisible = true
        binding.textError.isVisible = false
        binding.recyclerRecipes.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = MealDbClient.api.getMealsByCategory(categoryName)
                val meals = response.meals.orEmpty()
                if (meals.isEmpty()) {
                    binding.textError.setText(R.string.empty_recipes)
                    binding.textError.isVisible = true
                } else {
                    adapter.submitList(meals)
                    binding.recyclerRecipes.isVisible = true
                }
            } catch (_: Exception) {
                binding.textError.setText(R.string.error_loading_recipes)
                binding.textError.isVisible = true
            } finally {
                binding.progress.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_CATEGORY_NAME = "categoryName"
    }
}
