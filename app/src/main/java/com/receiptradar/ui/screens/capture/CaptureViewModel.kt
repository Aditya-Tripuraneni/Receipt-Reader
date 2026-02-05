package com.receiptradar.ui.screens.capture

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptradar.domain.model.ReceiptParseResult
import com.receiptradar.domain.usecase.ProcessReceiptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val processReceiptUseCase: ProcessReceiptUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()
    
    fun processImage(bitmap: Bitmap, imagePath: String?, imageUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            
            try {
                val result = processReceiptUseCase(bitmap, imagePath, imageUri)
                
                if (result.success && result.receiptId != null) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            processedReceiptId = result.receiptId,
                            parseResult = result.parseResult
                        )
                    }
                } else {
                    val errorMessage = result.parseResult.errors.firstOrNull()
                        ?: "Failed to process receipt"
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = errorMessage
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }
    
    fun onImageCaptured(uri: Uri?) {
        _uiState.update { it.copy(capturedImageUri = uri) }
    }
    
    fun clearState() {
        _uiState.value = CaptureUiState()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CaptureUiState(
    val capturedImageUri: Uri? = null,
    val isProcessing: Boolean = false,
    val processedReceiptId: Long? = null,
    val parseResult: ReceiptParseResult? = null,
    val error: String? = null
) {
    val hasProcessedReceipt: Boolean get() = processedReceiptId != null
}
