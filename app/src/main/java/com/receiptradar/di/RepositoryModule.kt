package com.receiptradar.di

import com.receiptradar.data.db.dao.InsightDao
import com.receiptradar.data.db.dao.ItemDao
import com.receiptradar.data.db.dao.ReceiptDao
import com.receiptradar.data.db.dao.CategoryDao
import com.receiptradar.data.repository.CategoryRepository
import com.receiptradar.data.repository.InsightRepository
import com.receiptradar.data.repository.ReceiptRepository
import com.receiptradar.domain.insight.InsightEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideReceiptRepository(
        receiptDao: ReceiptDao,
        itemDao: ItemDao
    ): ReceiptRepository {
        return ReceiptRepository(receiptDao, itemDao)
    }
    
    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository {
        return CategoryRepository(categoryDao)
    }
    
    @Provides
    @Singleton
    fun provideInsightRepository(
        insightDao: InsightDao,
        receiptDao: ReceiptDao,
        insightEngine: InsightEngine
    ): InsightRepository {
        return InsightRepository(insightDao, receiptDao, insightEngine)
    }
}
