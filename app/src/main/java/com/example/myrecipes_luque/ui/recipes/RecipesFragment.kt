package com.example.myrecipes_luque.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.FragmentRecipesBinding
import com.example.myrecipes_luque.ui.detail.RecipeDetailFragment
import com.example.myrecipes_luque.util.enableAppItemAnimations
import com.example.myrecipes_luque.util.revealIn
import com.example.myrecipes_luque.util.setupScrollToTop
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
        val filterName = requireArguments().getString(ARG_FILTER_NAME).orEmpty()
        val filterType = requireArguments().getString(ARG_FILTER_TYPE) ?: FILTER_TYPE_CATEGORY

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.recipes_screen_title, filterName)

        val spanCount = resources.getInteger(R.integer.list_span_count)
        binding.recyclerRecipes.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerRecipes.adapter = adapter
        binding.recyclerRecipes.enableAppItemAnimations()
        binding.recyclerRecipes.setupScrollToTop(
            fab = binding.fabScrollTop,
            threshold = resources.getInteger(R.integer.scroll_top_threshold),
        )
        binding.buttonRetry.setOnClickListener { loadRecipes(filterName, filterType) }
        loadRecipes(filterName, filterType)
    }

    private fun loadRecipes(filterName: String, filterType: String) {
        binding.progress.isVisible = true
        binding.stateContainer.isVisible = false
        binding.recyclerRecipes.isVisible = false
        binding.fabScrollTop.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = if (filterType == FILTER_TYPE_AREA) {
                    MealDbClient.api.getMealsByArea(filterName)
                } else {
                    MealDbClient.api.getMealsByCategory(filterName)
                }
                val meals = response.meals.orEmpty()
                if (meals.isEmpty()) {
                    binding.textState.setText(
                        if (filterType == FILTER_TYPE_AREA) {
                            R.string.empty_recipes_area
                        } else {
                            R.string.empty_recipes
                        },
                    )
                    binding.buttonRetry.isVisible = false
                    binding.stateContainer.revealIn()
                } else {
                    adapter.submitList(meals)
                    binding.recyclerRecipes.revealIn()
                }
            } catch (_: Exception) {
                binding.textState.setText(R.string.error_loading_recipes)
                binding.buttonRetry.isVisible = true
                binding.stateContainer.revealIn()
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
        const val ARG_FILTER_NAME = "filterName"
        const val ARG_FILTER_TYPE = "filterType"
        const val FILTER_TYPE_CATEGORY = "category"
        const val FILTER_TYPE_AREA = "area"

        /** @deprecated kept for older call sites */
        const val ARG_CATEGORY_NAME = ARG_FILTER_NAME
    }
}
