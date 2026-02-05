package com.receiptradar.data.db.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.receiptradar.data.db.entity.Insight
import com.receiptradar.data.db.entity.InsightReceiptJoin
import com.receiptradar.data.db.entity.Receipt

/**
 * POJO for fetching an Insight with all its related Receipts.
 */
data class InsightWithReceipts(
    @Embedded
    val insight: Insight,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = InsightReceiptJoin::class,
            parentColumn = "insightId",
            entityColumn = "receiptId"
        )
    )
    val receipts: List<Receipt>
)
