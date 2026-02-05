package com.receiptradar.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Category entity - represents a spending category.
 * Seeded with default categories on first run.
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Category name (unique) */
    val name: String,
    
    /** Icon resource name or asset path */
    val icon: String? = null,
    
    /** ARGB color for UI display */
    val color: Int? = null
) {
    companion object {
        /** Default categories to seed on first run */
        val DEFAULT_CATEGORIES = listOf(
            Category(name = "Food & Dining", icon = "restaurant", color = 0xFFFF6B6B.toInt()),
            Category(name = "Groceries", icon = "shopping_cart", color = 0xFF45B7D1.toInt()),
            Category(name = "Transport", icon = "directions_car", color = 0xFF4ECDC4.toInt()),
            Category(name = "Shopping", icon = "shopping_bag", color = 0xFFDDA0DD.toInt()),
            Category(name = "Entertainment", icon = "movie", color = 0xFFFFD93D.toInt()),
            Category(name = "Subscriptions", icon = "subscriptions", color = 0xFF96CEB4.toInt()),
            Category(name = "Utilities", icon = "bolt", color = 0xFF6BCB77.toInt()),
            Category(name = "Healthcare", icon = "local_hospital", color = 0xFFE57373.toInt()),
            Category(name = "Education", icon = "school", color = 0xFF64B5F6.toInt()),
            Category(name = "Other", icon = "category", color = 0xFFA8A8A8.toInt())
        )
    }
}
