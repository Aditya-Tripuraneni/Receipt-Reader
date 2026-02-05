package com.receiptradar.data.db.dao

import androidx.room.*
import com.receiptradar.data.db.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Category entity operations.
 */
@Dao
interface CategoryDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>): List<Long>
    
    @Update
    suspend fun update(category: Category)
    
    @Delete
    suspend fun delete(category: Category)
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAll(): List<Category>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?
    
    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getByName(name: String): Category?
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int
}
