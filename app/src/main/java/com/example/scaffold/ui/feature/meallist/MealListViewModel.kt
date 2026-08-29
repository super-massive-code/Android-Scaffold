package com.example.scaffold.ui.feature.meallist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealListViewModel
    @Inject
    constructor(
        private val mealRepository: MealRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<MealListUiState>(MealListUiState.Loading)
        val uiState: StateFlow<MealListUiState> = _uiState.asStateFlow()

        init {
            mealRepository
                .observeMeals()
                .onEach { meals ->
                    _uiState.update { current ->
                        when {
                            meals.isNotEmpty() -> MealListUiState.Content(meals)
                            current is MealListUiState.Error -> current
                            else -> MealListUiState.Loading
                        }
                    }
                }.launchIn(viewModelScope)
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                runCatching { mealRepository.refresh() }
                    .onFailure { throwable ->
                        if (_uiState.value !is MealListUiState.Content) {
                            _uiState.value = MealListUiState.Error(throwable.message)
                        }
                    }
            }
        }
    }
