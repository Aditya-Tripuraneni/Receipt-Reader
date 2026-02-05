package com.receiptradar.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Item entity - represents a single line item on a receipt.
 * Has a foreign key relationship to Receipt with cascade delete.
 */
@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["receiptId"]),
        Index(value = ["categoryId"])
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Foreign key to Receipt */
    val receiptId: Long,
    
    /** Item name/description */
    val name: String,
    
    /** Price in smallest currency unit (e.g., cents/paise) */
    val price: Long,
    
    /** Quantity (default 1) */
    val quantity: Double = 1.0,
    
    /** Foreign key to Category (nullable until categorized) */
    val categoryId: Long? = null,
    
    /** Order of item on receipt (0-based) */
    val sortOrder: Int = 0,
    
    /** Parse confidence score (0.0 to 1.0) */
    val confidence: Float? = null
)
