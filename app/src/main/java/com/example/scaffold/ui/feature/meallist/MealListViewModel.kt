package com.example.scaffold.ui.feature.meallist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.model.MealCategory
import com.example.scaffold.ui.components.toUiError
import com.example.scaffold.util.runSuspendCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Whether the cache we're observing has been given a chance to fill yet. [InFlight] is the
 * starting value because the ViewModel refreshes as soon as it's constructed, so an empty
 * cache before that first refresh lands is still "loading", not "empty".
 */
private sealed interface RefreshStatus {
    data object Idle : RefreshStatus

    data object InFlight : RefreshStatus

    data class Failed(
        val throwable: Throwable,
    ) : RefreshStatus
}

@HiltViewModel
class MealListViewModel
    @Inject
    constructor(
        private val mealRepository: MealRepository,
    ) : ViewModel() {
        private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.InFlight)
        private var refreshJob: Job? = null
        private val selectedCategory = MutableStateFlow(MealCategory.Chicken)

        val uiState: StateFlow<MealListUiState> =
            combine(
                mealRepository.observeMeals(),
                refreshStatus,
                selectedCategory,
            ) { meals, status, category ->
                when {
                    meals.isNotEmpty() ->
                        MealListUiState.Content(
                            meals = meals,
                            selectedCategory = category,
                            transientError = (status as? RefreshStatus.Failed)?.throwable?.toUiError(),
                        )
                    status is RefreshStatus.Failed -> MealListUiState.Error(status.throwable.toUiError())
                    status is RefreshStatus.InFlight -> MealListUiState.Loading
                    else -> MealListUiState.Content(meals, category)
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, MealListUiState.Loading)

        init {
            selectCategory(selectedCategory.value)
        }

        /** Called once the snackbar carrying `Content.transientError` has been shown. */
        fun dismissTransientError() {
            refreshStatus.update { status -> if (status is RefreshStatus.Failed) RefreshStatus.Idle else status }
        }

        fun refresh() {
            selectCategory(selectedCategory.value)
        }

        fun selectCategory(category: MealCategory) {
            selectedCategory.value = category
            refreshStatus.value = RefreshStatus.InFlight
            // Rapid category taps would otherwise interleave two cache replacements, and the
            // slower one would win.
            refreshJob?.cancel()
            refreshJob =
                viewModelScope.launch {
                    runSuspendCatching { mealRepository.refreshByCategory(category) }
                        .onSuccess { refreshStatus.value = RefreshStatus.Idle }
                        .onFailure { throwable -> refreshStatus.value = RefreshStatus.Failed(throwable) }
                }
        }
    }
