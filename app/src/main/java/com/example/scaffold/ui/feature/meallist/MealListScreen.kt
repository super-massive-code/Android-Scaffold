package com.example.scaffold.ui.feature.meallist

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.scaffold.ui.components.ErrorState
import com.example.scaffold.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealListScreen(
    onMealClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_meals)) }) },
    ) { padding ->
        when (val state = uiState) {
            MealListUiState.Loading -> LoadingIndicator(Modifier.padding(padding))
            is MealListUiState.Error ->
                ErrorState(
                    message = state.message ?: stringResource(R.string.meal_list_error_fallback),
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(padding),
                )
            is MealListUiState.Content ->
                Column(modifier = Modifier.padding(padding)) {
                    CategoryPicker(
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = viewModel::selectCategory,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    MealList(
                        meals = state.meals,
                        onMealClick = onMealClick,
                        modifier = Modifier.weight(1f),
                    )
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

@Composable
private fun MealList(
    meals: List<Meal>,
    onMealClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        items(items = meals, key = { it.id }) { meal ->
            MealRow(meal = meal, onClick = { onMealClick(meal.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealRow(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
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
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
            )
            Text(
                text = meal.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}
