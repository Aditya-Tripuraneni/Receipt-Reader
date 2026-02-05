package com.receiptradar.data.repository

import com.receiptradar.data.db.dao.InsightDao
import com.receiptradar.data.db.dao.ReceiptDao
import com.receiptradar.data.db.entity.Insight
import com.receiptradar.data.db.entity.InsightReceiptJoin
import com.receiptradar.data.db.entity.InsightType
import com.receiptradar.data.db.relation.InsightWithReceipts
import com.receiptradar.domain.insight.InsightEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Insight operations.
 */
@Singleton
class InsightRepository @Inject constructor(
    private val insightDao: InsightDao,
    private val receiptDao: ReceiptDao,
    private val insightEngine: InsightEngine
) {
    
    /**
     * Refresh all insights by recomputing from receipts.
     */
    suspend fun refreshInsights() = withContext(Dispatchers.IO) {
        // Get all receipts
        val receipts = receiptDao.getAll()
        
        if (receipts.isEmpty()) {
            insightDao.deleteAll()
            return@withContext
        }
        
        // Compute new insights
        val newInsights = insightEngine.computeInsights(receipts)
        
        // Replace all insights
        insightDao.deleteAll()
        
        for (insight in newInsights) {
            val insightId = insightDao.insert(insight.insight)
            
            // Insert joins if there are related receipts
            if (insight.receiptIds.isNotEmpty()) {
                val joins = insight.receiptIds.map { receiptId ->
                    InsightReceiptJoin(insightId = insightId, receiptId = receiptId)
                }
                insightDao.insertJoins(joins)
            }
        }
    }
    
    /**
     * Get all insights as a Flow (reactive).
     */
    fun getInsights(): Flow<List<Insight>> = insightDao.getAllFlow()
    
    /**
     * Get recent insights.
     */
    fun getRecentInsights(limit: Int = 10): Flow<List<Insight>> = insightDao.getRecent(limit)
    
    /**
     * Get insights by type.
     */
    fun getInsightsByType(type: InsightType): Flow<List<Insight>> =
        insightDao.getByType(type.value)
    
    /**
     * Get top insights by confidence.
     */
    suspend fun getTopInsights(limit: Int = 5): List<Insight> = withContext(Dispatchers.IO) {
        insightDao.getTopByConfidence(limit)
    }
    
    /**
     * Get insight with related receipts.
     */
    suspend fun getInsightWithReceipts(id: Long): InsightWithReceipts? = withContext(Dispatchers.IO) {
        insightDao.getInsightWithReceipts(id)
    }
    
    /**
     * Get all insights with receipts.
     */
    fun getAllInsightsWithReceipts(): Flow<List<InsightWithReceipts>> =
        insightDao.getAllInsightsWithReceipts()
    
    /**
     * Delete all insights.
     */
    suspend fun deleteAllInsights() = withContext(Dispatchers.IO) {
        insightDao.deleteAll()
    }
}
