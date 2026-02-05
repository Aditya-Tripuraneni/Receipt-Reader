package com.receiptradar.worker

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.receiptradar.data.repository.ReceiptRepository
import com.receiptradar.domain.pipeline.ReceiptProcessor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker for processing receipt images.
 * Used for batch processing or when processing needs to continue after app is closed.
 */
@HiltWorker
class ParseReceiptWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val receiptProcessor: ReceiptProcessor,
    private val receiptRepository: ReceiptRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val imageUri = inputData.getString(KEY_IMAGE_URI) ?: return@withContext Result.failure()
        
        try {
            // Load bitmap from URI
            val uri = Uri.parse(imageUri)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                return@withContext Result.failure(
                    workDataOf(KEY_ERROR to "Failed to load image")
                )
            }
            
            // Process the receipt (pass URI for EXIF orientation)
            val parseResult = receiptProcessor.process(bitmap, imageUri, uri)
            
            if (!parseResult.isSuccessful || parseResult.receipt == null) {
                val error = parseResult.errors.firstOrNull() ?: "Processing failed"
                return@withContext Result.failure(
                    workDataOf(KEY_ERROR to error)
                )
            }
            
            // Save to database
            val receiptId = receiptRepository.saveParseResult(parseResult)
            
            Result.success(
                workDataOf(
                    KEY_RECEIPT_ID to receiptId,
                    KEY_ITEM_COUNT to parseResult.items.size
                )
            )
            
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf(KEY_ERROR to (e.message ?: "Unknown error"))
                )
            }
        }
    }
    
    companion object {
        const val KEY_IMAGE_URI = "image_uri"
        const val KEY_RECEIPT_ID = "receipt_id"
        const val KEY_ITEM_COUNT = "item_count"
        const val KEY_ERROR = "error"
        private const val MAX_RETRIES = 3
        
        /**
         * Create a one-time work request for processing a receipt.
         */
        fun createWorkRequest(imageUri: String): OneTimeWorkRequest {
            val inputData = workDataOf(KEY_IMAGE_URI to imageUri)
            
            return OneTimeWorkRequestBuilder<ParseReceiptWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10_000L,
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()
        }
    }
}
