package com.receiptradar.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.receiptradar.data.db.dao.CategoryDao
import com.receiptradar.data.db.dao.InsightDao
import com.receiptradar.data.db.dao.ItemDao
import com.receiptradar.data.db.dao.ReceiptDao
import com.receiptradar.data.db.entity.Category
import com.receiptradar.data.db.entity.Insight
import com.receiptradar.data.db.entity.InsightReceiptJoin
import com.receiptradar.data.db.entity.Item
import com.receiptradar.data.db.entity.Receipt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room database for Receipt Radar app.
 */
@Database(
    entities = [
        Receipt::class,
        Item::class,
        Category::class,
        Insight::class,
        InsightReceiptJoin::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun receiptDao(): ReceiptDao
    abstract fun itemDao(): ItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun insightDao(): InsightDao
    
    companion object {
        private const val DATABASE_NAME = "receipt_radar.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default categories on first creation
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).categoryDao()
                                .insertAll(Category.DEFAULT_CATEGORIES)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
