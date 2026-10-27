package com.example.myrecipes_luque.ui.categories

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
import com.example.myrecipes_luque.databinding.FragmentCategoriesBinding
import com.example.myrecipes_luque.ui.recipes.RecipesFragment
import com.example.myrecipes_luque.util.setupScrollToTop
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val adapter = CategoriesAdapter { category ->
        val args = Bundle().apply {
            putString(RecipesFragment.ARG_CATEGORY_NAME, category.name)
        }
        findNavController().navigate(
            R.id.action_categoriesFragment_to_recipesFragment,
            args,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spanCount = resources.getInteger(R.integer.list_span_count)
        binding.recyclerCategories.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerCategories.adapter = adapter
        binding.recyclerCategories.setupScrollToTop(
            fab = binding.fabScrollTop,
            threshold = resources.getInteger(R.integer.scroll_top_threshold),
        )
        loadCategories()
    }

    private fun loadCategories() {
        binding.progress.isVisible = true
        binding.textError.isVisible = false
        binding.recyclerCategories.isVisible = false
        binding.fabScrollTop.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = MealDbClient.api.getCategories()
                val categories = response.categories.orEmpty()
                adapter.submitList(categories)
                binding.recyclerCategories.isVisible = true
            } catch (_: Exception) {
                binding.textError.setText(R.string.error_loading_categories)
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
}
