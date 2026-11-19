package com.example.myrecipes_luque.ui.areas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.FragmentAreasBinding
import com.example.myrecipes_luque.ui.recipes.RecipesFragment
import com.example.myrecipes_luque.util.setupScrollToTop
import kotlinx.coroutines.launch

class AreasFragment : Fragment() {

    private var _binding: FragmentAreasBinding? = null
    private val binding get() = _binding!!

    private val adapter = AreasAdapter { area ->
        val args = Bundle().apply {
            putString(RecipesFragment.ARG_FILTER_NAME, area.name)
            putString(RecipesFragment.ARG_FILTER_TYPE, RecipesFragment.FILTER_TYPE_AREA)
        }
        findNavController().navigate(
            R.id.action_areasFragment_to_recipesFragment,
            args,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAreasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spanCount = resources.getInteger(R.integer.list_span_count)
        binding.recyclerAreas.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerAreas.adapter = adapter
        binding.recyclerAreas.setupScrollToTop(
            fab = binding.fabScrollTop,
            threshold = resources.getInteger(R.integer.scroll_top_threshold),
        )
        binding.buttonRetry.setOnClickListener { loadAreas() }
        loadAreas()
    }

    private fun loadAreas() {
        binding.progress.isVisible = true
        binding.stateContainer.isVisible = false
        binding.recyclerAreas.isVisible = false
        binding.fabScrollTop.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val areas = MealDbClient.api.getAreas().meals.orEmpty()
                    .filter { it.name.isNotBlank() }
                    .sortedBy { it.name }
                if (areas.isEmpty()) {
                    binding.textState.setText(R.string.empty_areas)
                    binding.buttonRetry.isVisible = false
                    binding.stateContainer.isVisible = true
                } else {
                    adapter.submitList(areas)
                    binding.recyclerAreas.isVisible = true
                }
            } catch (_: Exception) {
                binding.textState.setText(R.string.error_loading_areas)
                binding.buttonRetry.isVisible = true
                binding.stateContainer.isVisible = true
            } finally {
                binding.progress.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
