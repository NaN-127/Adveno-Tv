package com.adveno.tv.feature.initialization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adveno.tv.domain.model.AppLaunchDestination
import com.adveno.tv.domain.usecase.DetermineAppLaunchDestinationUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface InitializationUiState {
    data object Loading : InitializationUiState
    data object NavigateToPairing : InitializationUiState
    data object NavigateToPlayback : InitializationUiState
    data class Error(val message: String) : InitializationUiState
}

class InitializationViewModel(
    private val determineAppLaunchDestinationUseCase: DetermineAppLaunchDestinationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InitializationUiState>(InitializationUiState.Loading)
    val uiState: StateFlow<InitializationUiState> = _uiState.asStateFlow()

    init {
        initializeApplication()
    }

    fun initializeApplication() {
        viewModelScope.launch {
            _uiState.value = InitializationUiState.Loading
            // Provide smooth brand animation time
            delay(1200)
            when (val destination = determineAppLaunchDestinationUseCase()) {
                is AppLaunchDestination.Playback -> {
                    _uiState.value = InitializationUiState.NavigateToPlayback
                }
                is AppLaunchDestination.Pairing -> {
                    _uiState.value = InitializationUiState.NavigateToPairing
                }
                is AppLaunchDestination.Error -> {
                    _uiState.value = InitializationUiState.Error(destination.message)
                }
                is AppLaunchDestination.Loading -> {
                    _uiState.value = InitializationUiState.Loading
                }
            }
        }
    }
}

