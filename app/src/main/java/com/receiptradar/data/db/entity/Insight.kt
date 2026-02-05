package com.receiptradar.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Insight entity - represents a computed spending insight.
 * Examples: recurring charges, spending trends, category breakdowns.
 */
@Entity(
    tableName = "insights",
    indices = [
        Index(value = ["type"]),
        Index(value = ["createdAt"])
    ]
)
data class Insight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Type of insight (e.g., "recurring", "trend", "category_breakdown") */
    val type: String,
    
    /** Human-readable description shown to user */
    val description: String,
    
    /** Confidence score (0.0 to 1.0) */
    val confidence: Float,
    
    /** JSON payload with type-specific data (receipt IDs, amounts, etc.) */
    val payload: String? = null,
    
    /** Timestamp when insight was computed */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Insight types as an enum for type-safe usage in code.
 */
enum class InsightType(val value: String) {
    RECURRING("recurring"),
    TREND("trend"),
    CATEGORY_BREAKDOWN("category_breakdown"),
    WEEKDAY("weekday"),
    MERCHANT_SUMMARY("merchant_summary"),
    TIME_OF_DAY("time_of_day"),
    TOP_SPENDING("top_spending");
    
    companion object {
        fun fromValue(value: String): InsightType? =
            entries.find { it.value == value }
    }
}
