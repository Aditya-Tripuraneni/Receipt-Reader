package com.receiptradar.domain.usecase

import com.receiptradar.data.db.entity.Insight
import com.receiptradar.data.repository.InsightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting and refreshing insights.
 */
class GetInsightsUseCase @Inject constructor(
    private val insightRepository: InsightRepository
) {
    
    /**
     * Get all insights as a flow.
     */
    operator fun invoke(): Flow<List<Insight>> = insightRepository.getInsights()
    
    /**
     * Refresh insights by recomputing from all receipts.
     */
    suspend fun refresh() {
        insightRepository.refreshInsights()
    }
    
    /**
     * Get top insights by confidence.
     */
    suspend fun getTopInsights(limit: Int = 5): List<Insight> {
        return insightRepository.getTopInsights(limit)
    }
}
