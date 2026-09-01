package com.example.scaffold.ui.feature.meallist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.model.MealCategory
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

        private var selectedCategory: MealCategory = MealCategory.Chicken

        init {
            mealRepository
                .observeMeals()
                .onEach { meals ->
                    _uiState.update { current ->
                        when {
                            meals.isNotEmpty() -> MealListUiState.Content(meals, selectedCategory)
                            current is MealListUiState.Error -> current
                            else -> MealListUiState.Loading
                        }
                    }
                }.launchIn(viewModelScope)
            selectCategory(selectedCategory)
        }

        fun refresh() {
            selectCategory(selectedCategory)
        }

        fun selectCategory(category: MealCategory) {
            selectedCategory = category
            viewModelScope.launch {
                runCatching { mealRepository.refreshByCategory(category) }
                    .onFailure { throwable ->
                        if (_uiState.value !is MealListUiState.Content) {
                            _uiState.value = MealListUiState.Error(throwable.message)
                        }
                    }
            }
        }
    }
