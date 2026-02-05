package com.receiptradar.data.repository

import com.receiptradar.data.db.dao.CategoryDao
import com.receiptradar.data.db.entity.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Category operations.
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    
    /**
     * Get all categories as a Flow (reactive).
     */
    fun getCategories(): Flow<List<Category>> = categoryDao.getAllFlow()
    
    /**
     * Get all categories synchronously.
     */
    suspend fun getCategoriesSync(): List<Category> = withContext(Dispatchers.IO) {
        categoryDao.getAll()
    }
    
    /**
     * Get a category by ID.
     */
    suspend fun getCategoryById(id: Long): Category? = withContext(Dispatchers.IO) {
        categoryDao.getById(id)
    }
    
    /**
     * Get a category by name.
     */
    suspend fun getCategoryByName(name: String): Category? = withContext(Dispatchers.IO) {
        categoryDao.getByName(name)
    }
    
    /**
     * Insert a new category.
     */
    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        categoryDao.insert(category)
    }
    
    /**
     * Update a category.
     */
    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.update(category)
    }
    
    /**
     * Delete a category.
     */
    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.delete(category)
    }
    
    /**
     * Ensure default categories are seeded.
     */
    suspend fun ensureDefaultCategories() = withContext(Dispatchers.IO) {
        if (categoryDao.getCount() == 0) {
            categoryDao.insertAll(Category.DEFAULT_CATEGORIES)
        }
    }
}
