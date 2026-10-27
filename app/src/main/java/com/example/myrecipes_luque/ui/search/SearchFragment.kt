package com.example.myrecipes_luque.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
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

    private val adapter = RecipesAdapter { meal ->
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
        val spanCount = resources.getInteger(R.integer.list_span_count)
        binding.recyclerSearch.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerSearch.adapter = adapter

        binding.editSearch.doAfterTextChanged { editable ->
            val query = editable?.toString().orEmpty().trim()
            scheduleSearch(query)
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
                binding.textMessage.setText(R.string.search_empty)
                binding.textMessage.isVisible = true
            } else {
                adapter.submitList(meals)
                binding.recyclerSearch.isVisible = true
            }
        } catch (_: Exception) {
            adapter.submitList(emptyList())
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
}
