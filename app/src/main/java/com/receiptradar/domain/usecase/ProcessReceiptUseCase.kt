package com.receiptradar.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.receiptradar.data.repository.ReceiptRepository
import com.receiptradar.domain.model.ReceiptParseResult
import com.receiptradar.domain.pipeline.ReceiptProcessor
import javax.inject.Inject

/**
 * Use case for processing a receipt image.
 * Orchestrates the pipeline and saves the result.
 */
class ProcessReceiptUseCase @Inject constructor(
    private val receiptProcessor: ReceiptProcessor,
    private val receiptRepository: ReceiptRepository
) {
    
    /**
     * Process a receipt image and save the result.
     * @param image The receipt image bitmap
     * @param imagePath Optional path where the image is stored
     * @param imageUri Optional URI for reading EXIF orientation data
     * @return The parse result with the saved receipt ID
     */
    suspend operator fun invoke(
        image: Bitmap, 
        imagePath: String? = null,
        imageUri: Uri? = null
    ): ProcessReceiptResult {
        // Run the processing pipeline
        val parseResult = receiptProcessor.process(image, imagePath, imageUri)
        
        if (!parseResult.isSuccessful || parseResult.receipt == null) {
            return ProcessReceiptResult(
                success = false,
                receiptId = null,
                parseResult = parseResult
            )
        }
        
        // Save to database
        val receiptId = receiptRepository.saveParseResult(parseResult)
        
        return ProcessReceiptResult(
            success = true,
            receiptId = receiptId,
            parseResult = parseResult
        )
    }
}

/**
 * Result of processing a receipt.
 */
data class ProcessReceiptResult(
    val success: Boolean,
    val receiptId: Long?,
    val parseResult: ReceiptParseResult
)
