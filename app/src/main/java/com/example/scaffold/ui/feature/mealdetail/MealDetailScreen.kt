package com.example.scaffold.ui.feature.mealdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.scaffold.R
import com.example.scaffold.model.Meal
import com.example.scaffold.ui.components.ErrorState
import com.example.scaffold.ui.components.LoadingIndicator
import com.example.scaffold.ui.components.sharedBoundsIfAvailable
import com.example.scaffold.ui.components.sharedElementIfAvailable
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MealDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealDetailViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedContentScope? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.meal_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = uiState,
            contentKey = { it::class },
            modifier = Modifier.padding(padding),
            label = "meal_detail_state",
        ) { state ->
            when (state) {
                MealDetailUiState.Loading -> LoadingIndicator()
                is MealDetailUiState.Error ->
                    ErrorState(
                        message = stringResource(state.error.messageRes),
                        onRetry = viewModel::refresh,
                    )
                is MealDetailUiState.Content ->
                    MealDetailBody(
                        meal = state.meal,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MealDetailBody(
    meal: Meal,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedContentScope?,
    modifier: Modifier = Modifier,
) {
    var instructionsVisible by remember(meal.id) { mutableStateOf(false) }
    LaunchedEffect(meal.id) {
        delay(INSTRUCTIONS_FADE_IN_DELAY_MILLIS)
        instructionsVisible = true
    }

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                    ).fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium),
        )
        Text(
            text = meal.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .sharedBoundsIfAvailable(
                        key = "meal-title-${meal.id}",
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
        )
        AnimatedVisibility(
            visible = instructionsVisible,
            enter = fadeIn(),
        ) {
            Text(
                text = meal.instructions.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

private const val INSTRUCTIONS_FADE_IN_DELAY_MILLIS = 150L
