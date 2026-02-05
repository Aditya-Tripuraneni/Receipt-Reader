package com.receiptradar.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Join table for N:M relationship between Insight and Receipt.
 * An insight can reference multiple receipts (e.g., recurring pattern across 3 receipts).
 */
@Entity(
    tableName = "insight_receipt_join",
    primaryKeys = ["insightId", "receiptId"],
    foreignKeys = [
        ForeignKey(
            entity = Insight::class,
            parentColumns = ["id"],
            childColumns = ["insightId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["insightId"]),
        Index(value = ["receiptId"])
    ]
)
data class InsightReceiptJoin(
    val insightId: Long,
    val receiptId: Long
)
