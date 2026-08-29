package com.example.scaffold.ui.feature.mealdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.ui.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class MealDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val mealRepository: MealRepository,
    ) : ViewModel() {
        private val mealId = savedStateHandle.toRoute<Destinations.MealDetail>().mealId

        val uiState: StateFlow<MealDetailUiState> =
            mealRepository
                .observeMeal(mealId)
                .map { meal ->
                    if (meal?.instructions != null) MealDetailUiState.Content(meal) else MealDetailUiState.Loading
                }.catch { throwable -> emit(MealDetailUiState.Error(throwable.message)) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = MealDetailUiState.Loading,
                )

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                runCatching { mealRepository.refresh(mealId) }
            }
        }
    }
