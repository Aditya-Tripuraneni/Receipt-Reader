package com.receiptradar.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.receiptradar.data.db.entity.Item
import com.receiptradar.data.db.entity.Receipt

/**
 * POJO for fetching a Receipt with all its Items in a single query.
 */
data class ReceiptWithItems(
    @Embedded
    val receipt: Receipt,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "receiptId"
    )
    val items: List<Item>
)
