package com.receiptradar.ui.screens.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptradar.data.db.entity.Receipt
import com.receiptradar.data.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptListViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReceiptListUiState())
    val uiState: StateFlow<ReceiptListUiState> = _uiState.asStateFlow()
    
    init {
        loadReceipts()
    }
    
    private fun loadReceipts() {
        viewModelScope.launch {
            receiptRepository.getAllReceipts()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { receipts ->
                    _uiState.update { 
                        it.copy(
                            receipts = receipts,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            try {
                receiptRepository.deleteReceipt(receipt)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ReceiptListUiState(
    val receipts: List<Receipt> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val isEmpty: Boolean get() = receipts.isEmpty() && !isLoading
}
