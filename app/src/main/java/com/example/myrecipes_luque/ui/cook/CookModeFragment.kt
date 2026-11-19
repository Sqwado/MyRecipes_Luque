package com.example.myrecipes_luque.ui.cook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.databinding.FragmentCookModeBinding

class CookModeFragment : Fragment() {

    private var _binding: FragmentCookModeBinding? = null
    private val binding get() = _binding!!

    private var pageCount: Int = 1

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateChrome(position)
        }
    }

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

        val ingredients = requireArguments().getStringArray(ARG_INGREDIENTS)?.toList().orEmpty()
        val ingredientNames = requireArguments().getStringArray(ARG_INGREDIENT_NAMES)?.toList()
            .orEmpty()
            .ifEmpty { ingredients }
        val steps = requireArguments().getStringArray(ARG_STEPS)?.toList().orEmpty()

        val mealName = requireArguments().getString(ARG_MEAL_NAME).orEmpty()
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            mealName.ifBlank { getString(R.string.cook_mode_title) }

        val pages = buildList {
            add(
                CookPage.Prep(
                    lines = ingredients.ifEmpty { listOf(getString(R.string.value_unavailable)) },
                    names = ingredientNames.ifEmpty {
                        listOf(getString(R.string.value_unavailable))
                    },
                ),
            )
            val stepBodies = steps.ifEmpty { listOf(getString(R.string.value_unavailable)) }
            stepBodies.forEachIndexed { index, body ->
                add(
                    CookPage.Step(
                        number = index + 1,
                        total = stepBodies.size,
                        body = body,
                    ),
                )
            }
        }
        pageCount = pages.size

        val adapter = CookPagerAdapter(pages)
        binding.pagerCook.adapter = adapter
        // Garde les pages en mémoire : le retour vers Préparer ne ré-inflate / re-Glide pas
        binding.pagerCook.offscreenPageLimit = pageCount.coerceAtLeast(1)

        binding.pagerCook.post {
            val recycler = binding.pagerCook.getChildAt(0) as? RecyclerView ?: return@post
            recycler.overScrollMode = View.OVER_SCROLL_NEVER
            recycler.itemAnimator = null
            recycler.setItemViewCacheSize(pageCount)
        }

        val initial = savedInstanceState?.getInt(STATE_PAGE_INDEX)
            ?.coerceIn(0, pageCount - 1)
            ?: 0
        binding.pagerCook.setCurrentItem(initial, false)
        updateChrome(initial)

        binding.pagerCook.registerOnPageChangeCallback(pageChangeCallback)

        binding.buttonPrevious.setOnClickListener {
            val current = binding.pagerCook.currentItem
            if (current > 0) {
                binding.pagerCook.setCurrentItem(current - 1, true)
            }
        }
        binding.buttonNext.setOnClickListener {
            val current = binding.pagerCook.currentItem
            if (current >= pageCount - 1) {
                // Pop instantané (nav_instant) — pas d’anim sur ViewPager lourd
                findNavController().navigateUp()
            } else {
                binding.pagerCook.setCurrentItem(current + 1, true)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let { outState.putInt(STATE_PAGE_INDEX, it.pagerCook.currentItem) }
    }

    private fun updateChrome(position: Int) {
        val progressPercent = if (pageCount <= 1) {
            100
        } else {
            ((position + 1) * 100) / pageCount
        }
        // Pas d’anim de progress pendant le swipe (évite la concurrence avec le pager)
        binding.progressCook.setProgressCompat(progressPercent, false)
        binding.progressCook.contentDescription = getString(
            R.string.cook_progress_a11y,
            position + 1,
            pageCount,
        )
        binding.textCookProgressLabel.text = getString(
            R.string.cook_progress_label,
            position + 1,
            pageCount,
        )
        binding.buttonPrevious.isEnabled = position > 0
        binding.buttonNext.setText(
            if (position >= pageCount - 1) {
                R.string.action_finish_cooking
            } else {
                R.string.action_next_step
            },
        )
    }

    override fun onDestroyView() {
        binding.pagerCook.unregisterOnPageChangeCallback(pageChangeCallback)
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
