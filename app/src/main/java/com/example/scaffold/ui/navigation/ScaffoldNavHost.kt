package com.example.scaffold.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun ScaffoldNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val isTopLevelDestination =
        TopLevelDestination.entries.any { topLevel ->
            currentDestination?.hierarchy?.any { it.hasRoute(topLevel.route::class) } == true
        }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (isTopLevelDestination) {
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
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.ContactList,
            modifier = Modifier.padding(padding),
        ) {
            composable<Destinations.ContactList> {
                ContactListScreen(onAddContactClick = { navController.navigate(Destinations.ContactForm) })
            }
            composable<Destinations.MealList> {
                MealListScreen(
                    onMealClick = { mealId -> navController.navigate(Destinations.MealDetail(mealId)) },
                )
            }
            composable<Destinations.MealDetail> {
                MealDetailScreen(onBack = { navController.popBackStack() })
            }
            composable<Destinations.ContactForm> {
                ContactFormScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
