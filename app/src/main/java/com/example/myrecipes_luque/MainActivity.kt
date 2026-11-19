package com.example.myrecipes_luque

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myrecipes_luque.data.remote.MealDbClient
import com.example.myrecipes_luque.databinding.ActivityMainBinding
import com.example.myrecipes_luque.ui.detail.RecipeDetailFragment
import com.example.myrecipes_luque.util.AppSnackbar
import com.example.myrecipes_luque.util.NavAnims
import com.example.myrecipes_luque.util.NetworkMonitor
import com.example.myrecipes_luque.util.slideBannerIn
import com.example.myrecipes_luque.util.slideBannerOut
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
                    val destinationId = menuItem.itemId
                    val handled = navController.graph.findNode(destinationId) != null
                    if (handled && navController.currentDestination?.id != destinationId) {
                        navController.navigate(destinationId, null, NavAnims.drawer(navController))
                    }
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
            if (online) binding.offlineBanner.slideBannerOut()
            else binding.offlineBanner.slideBannerIn()
        }
    }

    override fun onStop() {
        networkMonitor.stop()
        super.onStop()
    }

    private fun openSurpriseRecipe() {
        if (!networkMonitor.isOnline()) {
            AppSnackbar.show(
                host = binding.main,
                messageRes = R.string.error_loading_surprise,
                duration = Snackbar.LENGTH_LONG,
            )
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
                navController.navigate(
                    R.id.recipeDetailFragment,
                    args,
                    NavAnims.surpriseDetail(),
                )
            } catch (_: Exception) {
                AppSnackbar.show(
                    host = binding.main,
                    messageRes = R.string.error_loading_surprise,
                    duration = Snackbar.LENGTH_LONG,
                )
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
