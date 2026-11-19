package com.example.myrecipes_luque.util

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.myrecipes_luque.R

object NavAnims {

    fun forward(): NavOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.nav_enter)
        .setExitAnim(R.anim.nav_exit)
        .setPopEnterAnim(R.anim.nav_pop_enter)
        .setPopExitAnim(R.anim.nav_pop_exit)
        .build()

    fun drawer(navController: NavController): NavOptions = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setRestoreState(true)
        .setPopUpTo(navController.graph.startDestinationId, inclusive = false, saveState = true)
        .setEnterAnim(R.anim.nav_fade_in)
        .setExitAnim(R.anim.nav_fade_out)
        .setPopEnterAnim(R.anim.nav_fade_in)
        .setPopExitAnim(R.anim.nav_fade_out)
        .build()

    fun surpriseDetail(): NavOptions = NavOptions.Builder()
        .setLaunchSingleTop(false)
        .setPopUpTo(R.id.recipeDetailFragment, inclusive = true)
        .setEnterAnim(R.anim.nav_enter)
        .setExitAnim(R.anim.nav_exit)
        .setPopEnterAnim(R.anim.nav_pop_enter)
        .setPopExitAnim(R.anim.nav_pop_exit)
        .build()
}
