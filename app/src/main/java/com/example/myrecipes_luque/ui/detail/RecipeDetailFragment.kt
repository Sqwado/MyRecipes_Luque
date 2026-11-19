package com.example.myrecipes_luque.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.model.IngredientLine
import com.example.myrecipes_luque.data.model.MealDetail
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.FragmentRecipeDetailBinding
import com.example.myrecipes_luque.databinding.ItemIngredientBinding
import com.example.myrecipes_luque.databinding.ItemInstructionStepBinding
import com.example.myrecipes_luque.ui.cook.CookModeFragment
import com.example.myrecipes_luque.ui.favorites.FavoritesViewModel
import com.example.myrecipes_luque.util.InstructionParser
import com.example.myrecipes_luque.util.NutriScoreCalculator
import com.example.myrecipes_luque.util.ThumbLoader
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val favoritesViewModel: FavoritesViewModel by activityViewModels()

    private var currentMeal: MealDetail? = null
    private var isFavorite: Boolean = false
    private var instructionSteps: List<String> = emptyList()
    private var ingredientLines: List<IngredientLine> = emptyList()
    private var instructionsExpanded: Boolean = false
    private var ingredientsExpanded: Boolean = false

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
        ingredientsExpanded = savedInstanceState?.getBoolean(STATE_INGREDIENTS_EXPANDED) == true

        binding.buttonFavorite.isEnabled = false
        binding.buttonStartCooking.isEnabled = false
        binding.buttonShare.isEnabled = false
        binding.buttonFavorite.setOnClickListener { toggleFavorite() }
        binding.buttonStartCooking.setOnClickListener { startCooking() }
        binding.buttonShare.setOnClickListener { shareRecipe() }
        binding.buttonRetry.setOnClickListener { loadDetail(mealId) }
        binding.buttonToggleInstructions.setOnClickListener { toggleInstructions() }
        binding.buttonToggleIngredients.setOnClickListener { toggleIngredients() }
        binding.textIngredientsMore.setOnClickListener { toggleIngredients() }
        binding.chipYoutube.setOnClickListener { openYoutube() }

        observeFavoriteState(mealId)
        loadDetail(mealId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_INSTRUCTIONS_EXPANDED, instructionsExpanded)
        outState.putBoolean(STATE_INGREDIENTS_EXPANDED, ingredientsExpanded)
    }

    private fun observeFavoriteState(mealId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favoritesViewModel.observeIsFavorite(mealId).collect { favorite ->
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
        if (isFavorite) {
            favoritesViewModel.remove(meal.id)
            Snackbar.make(binding.root, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show()
        } else {
            favoritesViewModel.add(meal)
            Snackbar.make(binding.root, R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun shareRecipe() {
        val meal = currentMeal ?: return
        val category = meal.category?.takeIf { it.isNotBlank() }
            ?: getString(R.string.value_unavailable)
        val link = getString(R.string.mealdb_meal_page_base_url) + meal.id
        val text = getString(R.string.share_recipe_text, meal.name, category, link)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_recipe_subject, meal.name))
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_chooser_title)))
    }

    private fun openYoutube() {
        val url = currentMeal?.youtubeUrl?.takeIf { it.isNotBlank() } ?: return
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun startCooking() {
        val meal = currentMeal ?: return
        val unavailable = getString(R.string.value_unavailable)
        val ingredientNames = ingredientLines.map { it.name }
            .ifEmpty { listOf(unavailable) }
            .toTypedArray()
        val ingredients = ingredientLines.map { line ->
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
            putStringArray(CookModeFragment.ARG_INGREDIENT_NAMES, ingredientNames)
            putStringArray(CookModeFragment.ARG_STEPS, steps)
        }
        findNavController().navigate(
            R.id.action_recipeDetailFragment_to_cookModeFragment,
            args,
        )
    }

    private fun loadDetail(mealId: String) {
        binding.progress.isVisible = true
        binding.stateContainer.isVisible = false
        binding.contentScroll.isVisible = false
        binding.bottomBar.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val meal = MealDbClient.api.getMealById(mealId).meals?.firstOrNull()
                if (meal == null) {
                    binding.textError.setText(R.string.error_loading_recipe_detail)
                    binding.stateContainer.isVisible = true
                } else {
                    currentMeal = meal
                    bindMeal(meal)
                    updateFavoriteButton()
                    binding.buttonStartCooking.isEnabled = true
                    binding.buttonShare.isEnabled = true
                    binding.contentScroll.isVisible = true
                    binding.bottomBar.isVisible = true
                }
            } catch (_: Exception) {
                binding.textError.setText(R.string.error_loading_recipe_detail)
                binding.stateContainer.isVisible = true
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

        val youtube = meal.youtubeUrl?.takeIf { it.isNotBlank() }
        binding.chipYoutube.isVisible = youtube != null

        val nutriScore = NutriScoreCalculator.compute(meal)
        binding.nutriScoreView.setScorePercent(nutriScore)
        binding.textNutriScore.text = getString(R.string.nutri_score_value, nutriScore)

        ingredientLines = meal.ingredientLines()
        binding.textIngredientsCount.text =
            getString(R.string.ingredients_count, ingredientLines.size)
        renderIngredients()

        instructionSteps = InstructionParser.parse(meal.instructions)
        binding.textStepsCount.text = getString(R.string.steps_count, instructionSteps.size)
        renderInstructionSteps()
    }

    private fun toggleIngredients() {
        ingredientsExpanded = !ingredientsExpanded
        renderIngredients()
    }

    private fun renderIngredients() {
        val collapsedCount = resources.getInteger(R.integer.ingredients_collapsed_count)
        val visible = when {
            ingredientLines.isEmpty() -> emptyList()
            ingredientsExpanded || ingredientLines.size <= collapsedCount -> ingredientLines
            else -> ingredientLines.take(collapsedCount)
        }

        binding.containerIngredients.removeAllViews()
        val unavailable = getString(R.string.value_unavailable)
        val inflater = layoutInflater
        visible.forEach { line ->
            val itemBinding = ItemIngredientBinding.inflate(
                inflater,
                binding.containerIngredients,
                false,
            )
            itemBinding.textIngredient.text = line.name
            itemBinding.textMeasure.text = line.measure.ifBlank { unavailable }
            ThumbLoader.loadIngredient(itemBinding.imageIngredient, line.name)
            binding.containerIngredients.addView(itemBinding.root)
        }

        val canToggle = ingredientLines.size > collapsedCount
        binding.buttonToggleIngredients.isVisible = canToggle
        val remaining = (ingredientLines.size - collapsedCount).coerceAtLeast(0)
        val showMore = canToggle && !ingredientsExpanded && remaining > 0
        binding.textIngredientsMore.isVisible = showMore
        if (showMore) {
            binding.textIngredientsMore.text =
                getString(R.string.ingredients_more_count, remaining)
        }
        if (canToggle) {
            if (ingredientsExpanded) {
                binding.buttonToggleIngredients.setText(R.string.action_collapse_steps)
                binding.buttonToggleIngredients.setIconResource(R.drawable.ic_expand_less)
            } else {
                binding.buttonToggleIngredients.setText(R.string.action_expand_steps)
                binding.buttonToggleIngredients.setIconResource(R.drawable.ic_expand_more)
            }
        }
    }

    private fun toggleInstructions() {
        instructionsExpanded = !instructionsExpanded
        renderInstructionSteps()
    }

    private fun renderInstructionSteps() {
        val steps = instructionSteps.ifEmpty {
            listOf(getString(R.string.value_unavailable))
        }
        val maxLinesCollapsed = resources.getInteger(R.integer.instructions_collapsed_max_lines)

        binding.containerInstructionSteps.removeAllViews()
        val inflater = layoutInflater
        steps.forEachIndexed { index, step ->
            val itemBinding = ItemInstructionStepBinding.inflate(
                inflater,
                binding.containerInstructionSteps,
                false,
            )
            itemBinding.textStepIndex.text = getString(R.string.step_index_format, index + 1)
            itemBinding.textStepBody.text = step
            applyStepCompactState(itemBinding, instructionsExpanded, maxLinesCollapsed)
            itemBinding.root.setOnClickListener { toggleInstructions() }
            binding.containerInstructionSteps.addView(itemBinding.root)
        }

        binding.buttonToggleInstructions.isVisible = steps.isNotEmpty()
        if (instructionsExpanded) {
            binding.buttonToggleInstructions.setText(R.string.action_collapse_steps)
            binding.buttonToggleInstructions.setIconResource(R.drawable.ic_expand_less)
        } else {
            binding.buttonToggleInstructions.setText(R.string.action_expand_steps)
            binding.buttonToggleInstructions.setIconResource(R.drawable.ic_expand_more)
        }
    }

    private fun applyStepCompactState(
        itemBinding: ItemInstructionStepBinding,
        expanded: Boolean,
        maxLinesCollapsed: Int,
    ) {
        val body = itemBinding.textStepBody
        if (expanded) {
            body.maxLines = Int.MAX_VALUE
            body.ellipsize = null
            val pad = resources.getDimensionPixelSize(R.dimen.spacing_xxl)
            itemBinding.stepContent.setPadding(pad, pad, pad, pad)
        } else {
            body.maxLines = maxLinesCollapsed
            body.ellipsize = TextUtils.TruncateAt.END
            val h = resources.getDimensionPixelSize(R.dimen.spacing_lg)
            val v = resources.getDimensionPixelSize(R.dimen.spacing_md)
            itemBinding.stepContent.setPadding(h, v, h, v)
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
        private const val STATE_INGREDIENTS_EXPANDED = "ingredientsExpanded"
    }
}
