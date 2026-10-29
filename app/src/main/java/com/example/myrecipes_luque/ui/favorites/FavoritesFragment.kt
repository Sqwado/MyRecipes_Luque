package com.example.myrecipes_luque.ui.favorites

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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myrecipes_luque.MyRecipesApp
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.data.local.FavoriteEntity
import com.example.myrecipes_luque.databinding.FragmentFavoritesBinding
import com.example.myrecipes_luque.ui.detail.RecipeDetailFragment
import com.example.myrecipes_luque.util.setupScrollToTop
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val favoritesRepository by lazy {
        (requireActivity().application as MyRecipesApp).favoritesRepository
    }

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
        binding.recyclerFavorites.setupScrollToTop(
            fab = binding.fabScrollTop,
            threshold = resources.getInteger(R.integer.scroll_top_threshold),
        )
        observeFavorites()
    }

    private fun observeFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favoritesRepository.observeAll().collect { favorites ->
                    adapter.submitList(favorites)
                    binding.textEmpty.isVisible = favorites.isEmpty()
                    binding.recyclerFavorites.isVisible = favorites.isNotEmpty()
                    if (favorites.isEmpty()) {
                        binding.fabScrollTop.isVisible = false
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
        viewLifecycleOwner.lifecycleScope.launch {
            favoritesRepository.remove(favorite.mealId)
            Snackbar.make(binding.root, R.string.favorite_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        favoritesRepository.restore(favorite)
                    }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
