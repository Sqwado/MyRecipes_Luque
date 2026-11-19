package com.example.myrecipes_luque

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.ActivityMainBinding
import com.example.myrecipes_luque.ui.detail.RecipeDetailFragment
import com.example.myrecipes_luque.util.NetworkMonitor
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        networkMonitor = NetworkMonitor(this)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.categoriesFragment,
                R.id.areasFragment,
                R.id.searchFragment,
                R.id.favoritesFragment,
            ),
            binding.drawerLayout,
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_surprise -> {
                    binding.drawerLayout.closeDrawers()
                    openSurpriseRecipe()
                    true
                }
                else -> {
                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) {
                        binding.drawerLayout.closeDrawers()
                    }
                    handled
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val menu = binding.navView.menu
            val match = menu.findItem(destination.id)
            if (match != null) {
                match.isChecked = true
            } else {
                menu.findItem(R.id.nav_surprise)?.isChecked = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        networkMonitor.start { online ->
            binding.offlineBanner.isVisible = !online
        }
    }

    override fun onStop() {
        networkMonitor.stop()
        super.onStop()
    }

    private fun openSurpriseRecipe() {
        if (!networkMonitor.isOnline()) {
            Snackbar.make(binding.main, R.string.error_loading_surprise, Snackbar.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch {
            try {
                val meal = MealDbClient.api.getRandomMeal().meals?.firstOrNull()
                if (meal == null) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.error_loading_surprise,
                        Toast.LENGTH_SHORT,
                    ).show()
                    return@launch
                }
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController
                val args = bundleOf(
                    RecipeDetailFragment.ARG_MEAL_ID to meal.id,
                    RecipeDetailFragment.ARG_MEAL_NAME to meal.name,
                )
                // pop le détail courant pour forcer un nouveau fragment à chaque Surprise
                val options = NavOptions.Builder()
                    .setLaunchSingleTop(false)
                    .setPopUpTo(R.id.recipeDetailFragment, inclusive = true)
                    .build()
                navController.navigate(R.id.recipeDetailFragment, args, options)
            } catch (_: Exception) {
                Snackbar.make(binding.main, R.string.error_loading_surprise, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
