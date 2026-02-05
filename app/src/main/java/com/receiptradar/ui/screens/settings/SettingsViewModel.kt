package com.receiptradar.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptradar.data.repository.InsightRepository
import com.receiptradar.data.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val insightRepository: InsightRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadStats()
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            try {
                val receiptCount = receiptRepository.getReceiptCount()
                _uiState.update { it.copy(receiptCount = receiptCount) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun deleteAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                receiptRepository.deleteAllReceipts()
                insightRepository.deleteAllInsights()
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        receiptCount = 0,
                        deleteSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun clearDeleteSuccess() {
        _uiState.update { it.copy(deleteSuccess = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SettingsUiState(
    val receiptCount: Int = 0,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)
