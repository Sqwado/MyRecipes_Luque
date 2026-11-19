package com.example.myrecipes_luque.ui.cook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.databinding.FragmentCookModeBinding
import com.example.myrecipes_luque.databinding.ItemCookIngredientBinding
import com.example.myrecipes_luque.util.ThumbLoader

class CookModeFragment : Fragment() {

    private var _binding: FragmentCookModeBinding? = null
    private val binding get() = _binding!!

    private var ingredients: List<String> = emptyList()
    private var ingredientNames: List<String> = emptyList()
    private var steps: List<String> = emptyList()
    private var pageIndex: Int = 0

    private val pageCount: Int
        get() = 1 + steps.size

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCookModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ingredients = requireArguments().getStringArray(ARG_INGREDIENTS)?.toList().orEmpty()
        ingredientNames = requireArguments().getStringArray(ARG_INGREDIENT_NAMES)?.toList()
            .orEmpty()
            .ifEmpty { ingredients }
        steps = requireArguments().getStringArray(ARG_STEPS)?.toList().orEmpty()
        pageIndex = savedInstanceState?.getInt(STATE_PAGE_INDEX) ?: 0

        val mealName = requireArguments().getString(ARG_MEAL_NAME).orEmpty()
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            mealName.ifBlank { getString(R.string.cook_mode_title) }

        binding.buttonPrevious.setOnClickListener {
            if (pageIndex > 0) {
                pageIndex -= 1
                renderPage()
            }
        }
        binding.buttonNext.setOnClickListener {
            if (pageIndex >= pageCount - 1) {
                findNavController().navigateUp()
            } else {
                pageIndex += 1
                renderPage()
            }
        }

        populateIngredients()
        renderPage()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_PAGE_INDEX, pageIndex)
    }

    private fun populateIngredients() {
        binding.containerCookIngredients.removeAllViews()
        val inflater = layoutInflater
        ingredients.forEachIndexed { index, line ->
            val itemBinding = ItemCookIngredientBinding.inflate(
                inflater,
                binding.containerCookIngredients,
                false,
            )
            itemBinding.checkIngredient.text = line
            val name = ingredientNames.getOrNull(index) ?: line
            ThumbLoader.loadIngredient(itemBinding.imageIngredient, name)
            binding.containerCookIngredients.addView(itemBinding.root)
        }
    }

    private fun renderPage() {
        val safeIndex = pageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        pageIndex = safeIndex

        val progressPercent = if (pageCount <= 1) {
            100
        } else {
            ((safeIndex + 1) * 100) / pageCount
        }
        binding.progressCook.setProgressCompat(progressPercent, true)
        binding.progressCook.contentDescription = getString(
            R.string.cook_progress_a11y,
            safeIndex + 1,
            pageCount,
        )
        binding.textCookProgressLabel.text = getString(
            R.string.cook_progress_label,
            safeIndex + 1,
            pageCount,
        )

        val isPrep = safeIndex == 0
        binding.containerCookIngredients.isVisible = isPrep
        binding.cardCookStep.isVisible = !isPrep
        binding.textCookBadge.isVisible = !isPrep

        if (isPrep) {
            binding.textCookPhase.setText(R.string.cook_prep_title)
            binding.textCookSubtitle.setText(R.string.cook_prep_subtitle)
            binding.textCookSubtitle.isVisible = true
        } else {
            val stepNumber = safeIndex
            binding.textCookBadge.text = getString(R.string.step_index_format, stepNumber)
            binding.textCookPhase.text = getString(
                R.string.cook_step_title,
                stepNumber,
                steps.size,
            )
            binding.textCookSubtitle.isVisible = false
            binding.textCookStep.text = steps.getOrElse(stepNumber - 1) {
                getString(R.string.value_unavailable)
            }
        }

        binding.buttonPrevious.isEnabled = safeIndex > 0
        binding.buttonNext.setText(
            if (safeIndex >= pageCount - 1) {
                R.string.action_finish_cooking
            } else {
                R.string.action_next_step
            },
        )

        binding.cookContentScroll.scrollTo(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_MEAL_NAME = "mealName"
        const val ARG_INGREDIENTS = "ingredients"
        const val ARG_INGREDIENT_NAMES = "ingredientNames"
        const val ARG_STEPS = "steps"
        private const val STATE_PAGE_INDEX = "pageIndex"
    }
}
