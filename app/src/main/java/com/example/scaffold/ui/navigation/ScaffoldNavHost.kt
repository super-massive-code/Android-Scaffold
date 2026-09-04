package com.example.scaffold.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scaffold.ui.feature.contactform.ContactFormScreen
import com.example.scaffold.ui.feature.contactlist.ContactListScreen
import com.example.scaffold.ui.feature.mealdetail.MealDetailScreen
import com.example.scaffold.ui.feature.meallist.MealListScreen

/**
 * No outer [androidx.compose.material3.Scaffold] here: the bottom nav bar is overlaid on top of
 * [NavHost] (see [BottomNavigationBarHeight]) rather than resizing around it, so [NavHost] itself
 * always fills the screen and the bar can fade in/out without ever perturbing the coordinate
 * space a screen transition (e.g. the meal list/detail shared element) is animating within.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ScaffoldNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val isTopLevelDestination =
        TopLevelDestination.entries.any { topLevel ->
            currentDestination?.hierarchy?.any { it.hasRoute(topLevel.route::class) } == true
        }

    Box(modifier = modifier.fillMaxSize()) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Destinations.ContactList,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable<Destinations.ContactList> {
                    ContactListScreen(
                        onAddContactClick = { navController.navigate(Destinations.ContactForm()) },
                        onContactClick = { contactId ->
                            navController.navigate(Destinations.ContactForm(contactId))
                        },
                        modifier = Modifier.padding(bottom = BottomNavigationBarHeight),
                    )
                }
                composable<Destinations.MealList> {
                    MealListScreen(
                        onMealClick = { mealId -> navController.navigate(Destinations.MealDetail(mealId)) },
                        modifier = Modifier.padding(bottom = BottomNavigationBarHeight),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
                composable<Destinations.MealDetail> {
                    MealDetailScreen(
                        onBack = { navController.popBackStack() },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
                composable<Destinations.ContactForm> {
                    ContactFormScreen(onBack = { navController.popBackStack() })
                }
            }
        }

        AnimatedVisibility(
            visible = isTopLevelDestination,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            NavigationBar {
                TopLevelDestination.entries.forEach { topLevel ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.hasRoute(topLevel.route::class) } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(topLevel.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(topLevel.icon, contentDescription = stringResource(topLevel.label)) },
                        label = { Text(stringResource(topLevel.label)) },
                    )
                }
            }
        }
    }
}
