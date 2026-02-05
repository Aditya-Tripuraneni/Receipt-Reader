package com.receiptradar.data.db.dao

import androidx.room.*
import com.receiptradar.data.db.entity.Insight
import com.receiptradar.data.db.entity.InsightReceiptJoin
import com.receiptradar.data.db.relation.InsightWithReceipts
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Insight entity operations.
 */
@Dao
interface InsightDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: Insight): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(insights: List<Insight>): List<Long>
    
    @Update
    suspend fun update(insight: Insight)
    
    @Delete
    suspend fun delete(insight: Insight)
    
    @Query("DELETE FROM insights")
    suspend fun deleteAll()
    
    @Query("DELETE FROM insights WHERE type = :type")
    suspend fun deleteByType(type: String)
    
    @Query("SELECT * FROM insights WHERE id = :id")
    suspend fun getById(id: Long): Insight?
    
    @Query("SELECT * FROM insights ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<Insight>>
    
    @Query("SELECT * FROM insights ORDER BY createdAt DESC")
    suspend fun getAll(): List<Insight>
    
    @Query("SELECT * FROM insights WHERE type = :type ORDER BY confidence DESC")
    fun getByType(type: String): Flow<List<Insight>>
    
    @Query("SELECT * FROM insights ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<Insight>>
    
    @Query("SELECT * FROM insights ORDER BY confidence DESC LIMIT :limit")
    suspend fun getTopByConfidence(limit: Int): List<Insight>
    
    // Join table operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertJoin(join: InsightReceiptJoin)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertJoins(joins: List<InsightReceiptJoin>)
    
    @Query("DELETE FROM insight_receipt_join WHERE insightId = :insightId")
    suspend fun deleteJoinsByInsightId(insightId: Long)
    
    @Transaction
    @Query("SELECT * FROM insights WHERE id = :id")
    suspend fun getInsightWithReceipts(id: Long): InsightWithReceipts?
    
    @Transaction
    @Query("SELECT * FROM insights ORDER BY createdAt DESC")
    fun getAllInsightsWithReceipts(): Flow<List<InsightWithReceipts>>
}
