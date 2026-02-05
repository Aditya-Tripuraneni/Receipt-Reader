package com.receiptradar.data.repository

import com.receiptradar.data.db.dao.ItemDao
import com.receiptradar.data.db.dao.ReceiptDao
import com.receiptradar.data.db.entity.Item
import com.receiptradar.data.db.entity.Receipt
import com.receiptradar.data.db.relation.ReceiptWithItems
import com.receiptradar.domain.model.ReceiptParseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Receipt and Item operations.
 * Single source of truth for receipt data.
 */
@Singleton
class ReceiptRepository @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val itemDao: ItemDao
) {
    
    /**
     * Save a parsed receipt result (receipt + items) in a transaction.
     * @return The ID of the inserted receipt
     */
    suspend fun saveParseResult(result: ReceiptParseResult): Long = withContext(Dispatchers.IO) {
        val receipt = result.receipt ?: throw IllegalArgumentException("Receipt cannot be null")
        
        val receiptId = receiptDao.insert(receipt)
        
        // Insert items with the new receipt ID
        val itemsWithReceiptId = result.items.mapIndexed { index, item ->
            item.copy(receiptId = receiptId, sortOrder = index)
        }
        itemDao.insertAll(itemsWithReceiptId)
        
        receiptId
    }
    
    /**
     * Get all receipts as a Flow (reactive).
     */
    fun getAllReceipts(): Flow<List<Receipt>> = receiptDao.getAllFlow()
    
    /**
     * Get a receipt with its items by ID.
     */
    fun getReceiptWithItems(id: Long): Flow<ReceiptWithItems?> =
        receiptDao.getReceiptWithItems(id)
    
    /**
     * Get all receipts with items.
     */
    fun getAllReceiptsWithItems(): Flow<List<ReceiptWithItems>> =
        receiptDao.getAllReceiptsWithItems()
    
    /**
     * Get receipts by date range.
     */
    fun getReceiptsByDateRange(from: Long, to: Long): Flow<List<Receipt>> =
        receiptDao.getByDateRange(from, to)
    
    /**
     * Get receipts by merchant name.
     */
    fun getReceiptsByMerchant(merchant: String): Flow<List<Receipt>> =
        receiptDao.getByMerchant(merchant)
    
    /**
     * Update an existing receipt.
     */
    suspend fun updateReceipt(receipt: Receipt) = withContext(Dispatchers.IO) {
        receiptDao.update(receipt)
    }
    
    /**
     * Delete a receipt (cascade deletes items).
     */
    suspend fun deleteReceipt(receipt: Receipt) = withContext(Dispatchers.IO) {
        receiptDao.delete(receipt)
    }
    
    /**
     * Delete a receipt by ID.
     */
    suspend fun deleteReceiptById(id: Long) = withContext(Dispatchers.IO) {
        receiptDao.deleteById(id)
    }
    
    /**
     * Delete all receipts.
     */
    suspend fun deleteAllReceipts() = withContext(Dispatchers.IO) {
        receiptDao.deleteAll()
    }
    
    /**
     * Get receipt count.
     */
    suspend fun getReceiptCount(): Int = withContext(Dispatchers.IO) {
        receiptDao.getCount()
    }
    
    /**
     * Get total spend in a date range.
     */
    suspend fun getTotalSpendInRange(from: Long, to: Long): Long = withContext(Dispatchers.IO) {
        receiptDao.getTotalSpendInRange(from, to) ?: 0L
    }
    
    /**
     * Get all unique merchants.
     */
    suspend fun getAllMerchants(): List<String> = withContext(Dispatchers.IO) {
        receiptDao.getAllMerchants()
    }
    
    /**
     * Get all receipts synchronously (for workers).
     */
    suspend fun getAllReceiptsSync(): List<Receipt> = withContext(Dispatchers.IO) {
        receiptDao.getAll()
    }
    
    // Item operations
    
    /**
     * Update an item.
     */
    suspend fun updateItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.update(item)
    }
    
    /**
     * Delete an item.
     */
    suspend fun deleteItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.delete(item)
    }
    
    /**
     * Update item category.
     */
    suspend fun updateItemCategory(itemId: Long, categoryId: Long?) = withContext(Dispatchers.IO) {
        itemDao.updateCategory(itemId, categoryId)
    }
    
    /**
     * Get items by receipt ID.
     */
    fun getItemsByReceiptId(receiptId: Long): Flow<List<Item>> =
        itemDao.getByReceiptId(receiptId)
    
    /**
     * Get items by category.
     */
    fun getItemsByCategory(categoryId: Long): Flow<List<Item>> =
        itemDao.getByCategoryId(categoryId)
    
    /**
     * Search items by name.
     */
    suspend fun searchItems(query: String): List<Item> = withContext(Dispatchers.IO) {
        itemDao.searchByName(query)
    }
}
