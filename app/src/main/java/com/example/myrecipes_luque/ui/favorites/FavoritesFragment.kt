package com.example.myrecipes_luque.ui.favorites

import android.os.Bundle
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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.local.FavoriteEntity
import com.example.myrecipes_luque.databinding.FragmentFavoritesBinding
import com.example.myrecipes_luque.ui.detail.RecipeDetailFragment
import com.example.myrecipes_luque.util.AppSnackbar
import com.example.myrecipes_luque.util.enableAppItemAnimations
import com.example.myrecipes_luque.util.revealIn
import com.example.myrecipes_luque.util.setupScrollToTop
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by activityViewModels()

    private val adapter = FavoritesAdapter(
        onFavoriteClick = { favorite -> openDetail(favorite) },
        onRemoveClick = { favorite -> removeFavorite(favorite) },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spanCount = resources.getInteger(R.integer.list_span_count)
        binding.recyclerFavorites.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerFavorites.adapter = adapter
        binding.recyclerFavorites.enableAppItemAnimations()
        binding.recyclerFavorites.setupScrollToTop(
            fab = binding.fabScrollTop,
            threshold = resources.getInteger(R.integer.scroll_top_threshold),
        )
        observeFavorites()
    }

    private fun observeFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { favorites ->
                    adapter.submitList(favorites)
                    if (favorites.isEmpty()) {
                        binding.recyclerFavorites.isVisible = false
                        binding.fabScrollTop.isVisible = false
                        binding.textEmpty.revealIn()
                    } else {
                        binding.textEmpty.isVisible = false
                        binding.recyclerFavorites.revealIn()
                    }
                }
            }
        }
    }

    private fun openDetail(favorite: FavoriteEntity) {
        val args = Bundle().apply {
            putString(RecipeDetailFragment.ARG_MEAL_ID, favorite.mealId)
            putString(RecipeDetailFragment.ARG_MEAL_NAME, favorite.name)
        }
        findNavController().navigate(
            R.id.action_favoritesFragment_to_recipeDetailFragment,
            args,
        )
    }

    private fun removeFavorite(favorite: FavoriteEntity) {
        viewModel.remove(favorite.mealId)
        AppSnackbar.show(
            host = binding.root,
            messageRes = R.string.favorite_removed,
            duration = Snackbar.LENGTH_LONG,
            actionRes = R.string.action_undo,
            onAction = { viewModel.restore(favorite) },
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
