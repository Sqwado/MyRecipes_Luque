package com.example.myrecipes_luque.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.model.MealSummary
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.FragmentSearchBinding
import com.example.myrecipes_luque.ui.detail.RecipeDetailFragment
import com.example.myrecipes_luque.ui.recipes.RecipesAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var searchJob: Job? = null
    private val scrollTopThreshold by lazy {
        resources.getInteger(R.integer.scroll_top_threshold)
    }

    private val adapter = RecipesAdapter { meal ->
        hideKeyboard()
        val args = Bundle().apply {
            putString(RecipeDetailFragment.ARG_MEAL_ID, meal.id)
            putString(RecipeDetailFragment.ARG_MEAL_NAME, meal.name)
        }
        findNavController().navigate(
            R.id.action_searchFragment_to_recipeDetailFragment,
            args,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupScrollToTop()
    }

    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(R.integer.list_span_count)
        val layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerSearch.layoutManager = layoutManager
        binding.recyclerSearch.adapter = adapter

        binding.recyclerSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0 && binding.editSearch.hasFocus()) {
                    hideKeyboard()
                }
                binding.fabScrollTop.isVisible =
                    layoutManager.findFirstVisibleItemPosition() > scrollTopThreshold
            }
        })
    }

    private fun setupSearch() {
        binding.editSearch.doAfterTextChanged { editable ->
            val query = editable?.toString().orEmpty().trim()
            scheduleSearch(query)
        }

        binding.editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return@setOnEditorActionListener false
            hideKeyboard()
            val query = binding.editSearch.text?.toString()?.trim().orEmpty()
            searchJob?.cancel()
            if (query.length >= resources.getInteger(R.integer.search_min_query_length)) {
                viewLifecycleOwner.lifecycleScope.launch { performSearch(query) }
            }
            true
        }
    }

    private fun setupScrollToTop() {
        binding.fabScrollTop.setOnClickListener { scrollToTop() }
    }

    private fun hideKeyboard() {
        val focus = binding.editSearch
        focus.clearFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focus.windowToken, 0)
    }

    /** Retour en haut à durée fixe, comme sur M2Tmdb. */
    private fun scrollToTop() {
        val recycler = binding.recyclerSearch
        val layoutManager = recycler.layoutManager as? LinearLayoutManager ?: return
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        if (firstVisible <= 0) return

        if (firstVisible > SCROLL_TOP_NEAR_ITEMS) {
            layoutManager.scrollToPositionWithOffset(SCROLL_TOP_NEAR_ITEMS, 0)
        }
        recycler.post {
            val scroller = object : LinearSmoothScroller(requireContext()) {
                override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                override fun calculateTimeForScrolling(dx: Int): Int = SCROLL_TOP_DURATION_MS
                override fun calculateTimeForDeceleration(dx: Int): Int = SCROLL_TOP_DURATION_MS
            }
            scroller.targetPosition = 0
            layoutManager.startSmoothScroll(scroller)
        }
    }

    private fun scheduleSearch(query: String) {
        searchJob?.cancel()
        val minLength = resources.getInteger(R.integer.search_min_query_length)
        val debounceMs = resources.getInteger(R.integer.search_debounce_ms).toLong()

        if (query.length < minLength) {
            adapter.submitList(emptyList())
            binding.progress.isVisible = false
            binding.recyclerSearch.isVisible = false
            binding.fabScrollTop.isVisible = false
            binding.textMessage.isVisible = true
            binding.textMessage.setText(
                if (query.isEmpty()) R.string.search_prompt else R.string.search_query_too_short,
            )
            return
        }

        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(debounceMs)
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        binding.progress.isVisible = true
        binding.textMessage.isVisible = false
        binding.recyclerSearch.isVisible = false

        try {
            val meals = MealDbClient.api.searchMealsByName(query).meals
                .orEmpty()
                .map { meal ->
                    MealSummary(
                        id = meal.id,
                        name = meal.name,
                        thumbUrl = meal.thumbUrl,
                    )
                }

            if (meals.isEmpty()) {
                adapter.submitList(emptyList())
                binding.fabScrollTop.isVisible = false
                binding.textMessage.setText(R.string.search_empty)
                binding.textMessage.isVisible = true
            } else {
                adapter.submitList(meals)
                binding.recyclerSearch.isVisible = true
            }
        } catch (_: Exception) {
            adapter.submitList(emptyList())
            binding.fabScrollTop.isVisible = false
            binding.textMessage.setText(R.string.error_loading_search)
            binding.textMessage.isVisible = true
        } finally {
            binding.progress.isVisible = false
        }
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val SCROLL_TOP_NEAR_ITEMS = 12
        const val SCROLL_TOP_DURATION_MS = 400
    }
}
