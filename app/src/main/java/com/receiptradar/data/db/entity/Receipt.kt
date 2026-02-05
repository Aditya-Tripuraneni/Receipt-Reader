package com.receiptradar.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Receipt entity - represents a single receipt with merchant, date, and total.
 * Items are stored separately with a foreign key relationship.
 */
@Entity(
    tableName = "receipts",
    indices = [
        Index(value = ["merchant"]),
        Index(value = ["date"]),
        Index(value = ["createdAt"])
    ]
)
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Merchant/store name extracted from receipt */
    val merchant: String,
    
    /** Receipt date in epoch milliseconds */
    val date: Long,
    
    /** Total amount in smallest currency unit (e.g., cents/paise) */
    val total: Long,
    
    /** ISO 4217 currency code (e.g., "INR", "USD") */
    val currency: String = "INR",
    
    /** Full OCR raw text output */
    val rawText: String? = null,
    
    /** Local file path or URI to the receipt image */
    val imagePath: String? = null,
    
    /** Timestamp when this receipt was added */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Aggregate parse confidence score (0.0 to 1.0) */
    val confidence: Float? = null,
    
    /** Optional user ID for future multi-user support */
    val userId: String? = null
)
