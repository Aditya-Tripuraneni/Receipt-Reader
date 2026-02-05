package com.receiptradar.data.db.dao

import androidx.room.*
import com.receiptradar.data.db.entity.Item
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Item entity operations.
 */
@Dao
interface ItemDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>): List<Long>
    
    @Update
    suspend fun update(item: Item)
    
    @Delete
    suspend fun delete(item: Item)
    
    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM items WHERE receiptId = :receiptId")
    suspend fun deleteByReceiptId(receiptId: Long)
    
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Long): Item?
    
    @Query("SELECT * FROM items WHERE receiptId = :receiptId ORDER BY sortOrder ASC")
    fun getByReceiptId(receiptId: Long): Flow<List<Item>>
    
    @Query("SELECT * FROM items WHERE receiptId = :receiptId ORDER BY sortOrder ASC")
    suspend fun getByReceiptIdSync(receiptId: Long): List<Item>
    
    @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY receiptId DESC")
    fun getByCategoryId(categoryId: Long): Flow<List<Item>>
    
    @Query("UPDATE items SET categoryId = :categoryId WHERE id = :itemId")
    suspend fun updateCategory(itemId: Long, categoryId: Long?)
    
    @Query("SELECT SUM(price * quantity) FROM items WHERE receiptId = :receiptId")
    suspend fun getTotalForReceipt(receiptId: Long): Long?
    
    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' ORDER BY receiptId DESC")
    suspend fun searchByName(query: String): List<Item>
}
