package com.receiptradar.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.receiptradar.data.repository.InsightRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background worker for computing spending insights.
 * Runs periodically to update insights based on new receipt data.
 */
@HiltWorker
class InsightComputeWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val insightRepository: InsightRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Refresh all insights
            insightRepository.refreshInsights()
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        const val WORK_NAME = "insight_compute_work"
        private const val MAX_RETRIES = 2
        
        /**
         * Create a periodic work request that runs every 6 hours.
         */
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<InsightComputeWorker>(
                6, TimeUnit.HOURS,
                30, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        }
        
        /**
         * Create a one-time work request for immediate insight refresh.
         */
        fun createOneTimeWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<InsightComputeWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        }
        
        /**
         * Enqueue periodic insight computation work.
         */
        fun enqueuePeriodicWork(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                createPeriodicWorkRequest()
            )
        }
        
        /**
         * Trigger immediate insight refresh.
         */
        fun triggerImmediateRefresh(context: Context) {
            WorkManager.getInstance(context).enqueue(createOneTimeWorkRequest())
        }
    }
}
