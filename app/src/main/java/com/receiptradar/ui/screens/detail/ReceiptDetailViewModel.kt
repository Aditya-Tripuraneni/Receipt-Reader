package com.receiptradar.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptradar.data.db.entity.Category
import com.receiptradar.data.db.entity.Item
import com.receiptradar.data.db.entity.Receipt
import com.receiptradar.data.db.relation.ReceiptWithItems
import com.receiptradar.data.repository.CategoryRepository
import com.receiptradar.data.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val receiptId: Long = savedStateHandle.get<Long>("receiptId") ?: 0L
    
    private val _uiState = MutableStateFlow(ReceiptDetailUiState())
    val uiState: StateFlow<ReceiptDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadReceipt()
        loadCategories()
    }
    
    private fun loadReceipt() {
        viewModelScope.launch {
            receiptRepository.getReceiptWithItems(receiptId)
                .filterNotNull()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { receiptWithItems ->
                    _uiState.update {
                        it.copy(
                            receipt = receiptWithItems.receipt,
                            items = receiptWithItems.items,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories()
                .collect { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
        }
    }
    
    fun updateItemCategory(itemId: Long, categoryId: Long?) {
        viewModelScope.launch {
            try {
                receiptRepository.updateItemCategory(itemId, categoryId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun updateItem(itemId: Long, name: String, price: Long, quantity: Double, categoryId: Long?) {
        viewModelScope.launch {
            try {
                val item = _uiState.value.items.find { it.id == itemId } ?: return@launch
                val updatedItem = item.copy(
                    name = name,
                    price = price,
                    quantity = quantity,
                    categoryId = categoryId
                )
                receiptRepository.updateItem(updatedItem)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            try {
                val item = _uiState.value.items.find { it.id == itemId } ?: return@launch
                receiptRepository.deleteItem(item)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun updateReceipt(merchant: String?, date: Long?, total: Long?) {
        val currentReceipt = _uiState.value.receipt ?: return
        
        viewModelScope.launch {
            try {
                val updatedReceipt = currentReceipt.copy(
                    merchant = merchant ?: currentReceipt.merchant,
                    date = date ?: currentReceipt.date,
                    total = total ?: currentReceipt.total
                )
                receiptRepository.updateReceipt(updatedReceipt)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun deleteReceipt() {
        val currentReceipt = _uiState.value.receipt ?: return
        
        viewModelScope.launch {
            try {
                receiptRepository.deleteReceipt(currentReceipt)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun showEditDialog(item: Item) {
        _uiState.update { it.copy(editingItem = item) }
    }
    
    fun hideEditDialog() {
        _uiState.update { it.copy(editingItem = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ReceiptDetailUiState(
    val receipt: Receipt? = null,
    val items: List<Item> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val editingItem: Item? = null,
    val error: String? = null
)
