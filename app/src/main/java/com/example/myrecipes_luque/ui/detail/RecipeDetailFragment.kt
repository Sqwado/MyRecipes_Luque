package com.example.myrecipes_luque.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myrecipes_luque.MyRecipesApp
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.model.MealDetail
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.FragmentRecipeDetailBinding
import com.example.myrecipes_luque.databinding.ItemIngredientBinding
import com.example.myrecipes_luque.databinding.ItemInstructionStepBinding
import com.example.myrecipes_luque.ui.cook.CookModeFragment
import com.example.myrecipes_luque.util.InstructionParser
import com.example.myrecipes_luque.util.NutriScoreCalculator
import com.example.myrecipes_luque.util.ThumbLoader
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val favoritesRepository by lazy {
        (requireActivity().application as MyRecipesApp).favoritesRepository
    }

    private var currentMeal: MealDetail? = null
    private var isFavorite: Boolean = false
    private var instructionSteps: List<String> = emptyList()
    private var instructionsExpanded: Boolean = false

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
        instructionsExpanded = savedInstanceState?.getBoolean(STATE_INSTRUCTIONS_EXPANDED) == true

        binding.buttonFavorite.isEnabled = false
        binding.buttonStartCooking.isEnabled = false
        binding.buttonFavorite.setOnClickListener { toggleFavorite() }
        binding.buttonStartCooking.setOnClickListener { startCooking() }
        binding.buttonToggleInstructions.setOnClickListener {
            instructionsExpanded = !instructionsExpanded
            renderInstructionSteps()
        }

        observeFavoriteState(mealId)
        loadDetail(mealId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_INSTRUCTIONS_EXPANDED, instructionsExpanded)
    }

    private fun observeFavoriteState(mealId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favoritesRepository.observeIsFavorite(mealId).collect { favorite ->
                    isFavorite = favorite
                    updateFavoriteButton()
                }
            }
        }
    }

    private fun updateFavoriteButton() {
        binding.buttonFavorite.setText(
            if (isFavorite) R.string.action_unfavorite_short else R.string.action_favorite_short,
        )
        binding.buttonFavorite.isEnabled = currentMeal != null
    }

    private fun toggleFavorite() {
        val meal = currentMeal ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            if (isFavorite) {
                favoritesRepository.remove(meal.id)
                Snackbar.make(binding.root, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show()
            } else {
                favoritesRepository.add(meal)
                Snackbar.make(binding.root, R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCooking() {
        val meal = currentMeal ?: return
        val unavailable = getString(R.string.value_unavailable)
        val ingredients = meal.ingredientLines().map { line ->
            if (line.measure.isBlank()) {
                line.name
            } else {
                getString(R.string.ingredient_line_format, line.name, line.measure)
            }
        }.ifEmpty { listOf(unavailable) }.toTypedArray()
        val steps = instructionSteps
            .ifEmpty { listOf(getString(R.string.value_unavailable)) }
            .toTypedArray()
        val args = Bundle().apply {
            putString(CookModeFragment.ARG_MEAL_NAME, meal.name)
            putStringArray(CookModeFragment.ARG_INGREDIENTS, ingredients)
            putStringArray(CookModeFragment.ARG_STEPS, steps)
        }
        findNavController().navigate(
            R.id.action_recipeDetailFragment_to_cookModeFragment,
            args,
        )
    }

    private fun loadDetail(mealId: String) {
        binding.progress.isVisible = true
        binding.textError.isVisible = false
        binding.contentScroll.isVisible = false
        binding.bottomBar.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val meal = MealDbClient.api.getMealById(mealId).meals?.firstOrNull()
                if (meal == null) {
                    binding.textError.setText(R.string.error_loading_recipe_detail)
                    binding.textError.isVisible = true
                } else {
                    currentMeal = meal
                    bindMeal(meal)
                    updateFavoriteButton()
                    binding.buttonStartCooking.isEnabled = true
                    binding.contentScroll.isVisible = true
                    binding.bottomBar.isVisible = true
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

        instructionSteps = InstructionParser.parse(meal.instructions)
        binding.textStepsCount.text = getString(R.string.steps_count, instructionSteps.size)
        binding.buttonToggleInstructions.isVisible =
            instructionSteps.size > resources.getInteger(R.integer.instructions_collapsed_count)
        renderInstructionSteps()

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

    private fun renderInstructionSteps() {
        val collapsedCount = resources.getInteger(R.integer.instructions_collapsed_count)
        val visibleSteps = when {
            instructionSteps.isEmpty() -> listOf(getString(R.string.value_unavailable))
            instructionsExpanded || instructionSteps.size <= collapsedCount -> instructionSteps
            else -> instructionSteps.take(collapsedCount)
        }

        binding.containerInstructionSteps.removeAllViews()
        val inflater = layoutInflater
        visibleSteps.forEachIndexed { index, step ->
            val itemBinding = ItemInstructionStepBinding.inflate(
                inflater,
                binding.containerInstructionSteps,
                false,
            )
            itemBinding.textStepIndex.text = getString(R.string.step_index_format, index + 1)
            itemBinding.textStepBody.text = step
            binding.containerInstructionSteps.addView(itemBinding.root)
        }

        val canToggle = instructionSteps.size > collapsedCount
        binding.buttonToggleInstructions.isVisible = canToggle
        if (canToggle) {
            if (instructionsExpanded) {
                binding.buttonToggleInstructions.setText(R.string.action_collapse_steps)
                binding.buttonToggleInstructions.setIconResource(R.drawable.ic_expand_less)
            } else {
                binding.buttonToggleInstructions.setText(R.string.action_expand_steps)
                binding.buttonToggleInstructions.setIconResource(R.drawable.ic_expand_more)
            }
        }
    }

    override fun onDestroyView() {
        ThumbLoader.clear(binding.imageHero)
        currentMeal = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_MEAL_ID = "mealId"
        const val ARG_MEAL_NAME = "mealName"
        private const val STATE_INSTRUCTIONS_EXPANDED = "instructionsExpanded"
    }
}
