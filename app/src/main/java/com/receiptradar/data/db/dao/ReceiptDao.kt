package com.receiptradar.data.db.dao

import androidx.room.*
import com.receiptradar.data.db.entity.Receipt
import com.receiptradar.data.db.relation.ReceiptWithItems
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Receipt entity operations.
 */
@Dao
interface ReceiptDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: Receipt): Long
    
    @Update
    suspend fun update(receipt: Receipt)
    
    @Delete
    suspend fun delete(receipt: Receipt)
    
    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM receipts")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getById(id: Long): Receipt?
    
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    fun getAllFlow(): Flow<List<Receipt>>
    
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAll(): List<Receipt>
    
    @Query("SELECT * FROM receipts WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<Receipt>>
    
    @Query("SELECT * FROM receipts WHERE merchant LIKE '%' || :merchant || '%' ORDER BY date DESC")
    fun getByMerchant(merchant: String): Flow<List<Receipt>>
    
    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :id")
    fun getReceiptWithItems(id: Long): Flow<ReceiptWithItems?>
    
    @Transaction
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    fun getAllReceiptsWithItems(): Flow<List<ReceiptWithItems>>
    
    @Query("SELECT COUNT(*) FROM receipts")
    suspend fun getCount(): Int
    
    @Query("SELECT SUM(total) FROM receipts WHERE date BETWEEN :from AND :to")
    suspend fun getTotalSpendInRange(from: Long, to: Long): Long?
    
    @Query("SELECT DISTINCT merchant FROM receipts ORDER BY merchant ASC")
    suspend fun getAllMerchants(): List<String>
}
