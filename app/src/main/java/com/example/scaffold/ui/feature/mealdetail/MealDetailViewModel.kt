package com.example.scaffold.ui.feature.mealdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.ui.components.toUiError
import com.example.scaffold.ui.navigation.Destinations
import com.example.scaffold.util.runSuspendCatching
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
class MealDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val mealRepository: MealRepository,
    ) : ViewModel() {
        private val mealId = savedStateHandle.toRoute<Destinations.MealDetail>().mealId

        private val _uiState = MutableStateFlow<MealDetailUiState>(MealDetailUiState.Loading)
        val uiState: StateFlow<MealDetailUiState> = _uiState.asStateFlow()

        init {
            mealRepository
                .observeMeal(mealId)
                .onEach { meal ->
                    _uiState.update { current ->
                        when {
                            meal?.instructions != null ->
                                MealDetailUiState.Content(
                                    meal = meal,
                                    transientError = (current as? MealDetailUiState.Content)?.transientError,
                                )
                            current is MealDetailUiState.Error -> current
                            else -> MealDetailUiState.Loading
                        }
                    }
                }.launchIn(viewModelScope)
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                runSuspendCatching { mealRepository.refresh(mealId) }
                    .onFailure { throwable ->
                        val error = throwable.toUiError()
                        _uiState.update { current ->
                            when (current) {
                                is MealDetailUiState.Content -> current.copy(transientError = error)
                                else -> MealDetailUiState.Error(error)
                            }
                        }
                    }
            }
        }

        /** Called once the snackbar carrying `Content.transientError` has been shown. */
        fun dismissTransientError() {
            _uiState.update { current ->
                if (current is MealDetailUiState.Content) current.copy(transientError = null) else current
            }
        }
    }
