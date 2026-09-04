package com.example.scaffold.ui.feature.meallist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.scaffold.R
import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import com.example.scaffold.ui.components.EmptyState
import com.example.scaffold.ui.components.ErrorState
import com.example.scaffold.ui.components.LoadingIndicator
import com.example.scaffold.ui.components.sharedBoundsIfAvailable
import com.example.scaffold.ui.components.sharedElementIfAvailable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MealListScreen(
    onMealClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealListViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedContentScope? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_meals)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = uiState,
            contentKey = { it::class },
            modifier = Modifier.padding(padding),
            label = "meal_list_state",
        ) { state ->
            when (state) {
                MealListUiState.Loading -> LoadingIndicator()
                is MealListUiState.Error ->
                    ErrorState(
                        message = stringResource(state.error.messageRes),
                        onRetry = viewModel::refresh,
                    )
                is MealListUiState.Content ->
                    Column {
                        CategoryPicker(
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = viewModel::selectCategory,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        MealList(
                            meals = state.meals,
                            onMealClick = onMealClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            modifier = Modifier.weight(1f),
                        )
                    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPicker(
    selectedCategory: MealCategory,
    onCategorySelected: (MealCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = MealCategory.entries, key = { it.name }) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(stringResource(category.label)) },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MealList(
    meals: List<Meal>,
    onMealClick: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedContentScope?,
    modifier: Modifier = Modifier,
) {
    if (meals.isEmpty()) {
        EmptyState(message = stringResource(R.string.meal_list_empty_message), modifier = modifier)
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        items(items = meals, key = { it.id }) { meal ->
            MealRow(
                meal = meal,
                onClick = { onMealClick(meal.id) },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun MealRow(
    meal: Meal,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedContentScope?,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier =
            modifier
                .padding(vertical = 6.dp)
                .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            AsyncImage(
                model = meal.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .sharedElementIfAvailable(
                            key = "meal-image-${meal.id}",
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        ).size(64.dp)
                        .clip(MaterialTheme.shapes.small),
            )
            Text(
                text = meal.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .sharedBoundsIfAvailable(
                            key = "meal-title-${meal.id}",
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
            )
        }
    }
}
