package com.receiptradar.domain.insight

import com.receiptradar.data.db.entity.InsightType
import com.receiptradar.data.db.entity.Receipt
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class InsightEngineTest {
    
    private lateinit var insightEngine: InsightEngine
    
    @Before
    fun setup() {
        insightEngine = InsightEngine()
    }
    
    @Test
    fun `computeInsights should return empty for no receipts`() {
        val result = insightEngine.computeInsights(emptyList())
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `computeInsights should detect recurring patterns`() {
        // Create receipts with monthly pattern
        val now = Calendar.getInstance()
        val receipts = (0..3).map { month ->
            val date = Calendar.getInstance().apply {
                add(Calendar.MONTH, -month)
            }
            createReceipt(
                id = month.toLong() + 1,
                merchant = "Netflix",
                total = 29900, // ₹299
                date = date.timeInMillis
            )
        }
        
        val result = insightEngine.computeInsights(receipts)
        
        val recurringInsights = result.filter { 
            it.insight.type == InsightType.RECURRING.value 
        }
        assertTrue(recurringInsights.isNotEmpty())
    }
    
    @Test
    fun `computeInsights should compute merchant summary`() {
        val receipts = (1..5).map { i ->
            createReceipt(
                id = i.toLong(),
                merchant = "Starbucks",
                total = 25000,
                date = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L)
            )
        }
        
        val result = insightEngine.computeInsights(receipts)
        
        val merchantInsights = result.filter {
            it.insight.type == InsightType.MERCHANT_SUMMARY.value
        }
        assertTrue(merchantInsights.isNotEmpty())
        assertTrue(merchantInsights.first().insight.description.contains("Starbucks"))
    }
    
    @Test
    fun `computeInsights should identify top spending`() {
        val receipts = listOf(
            createReceipt(1, "Store A", 10000),
            createReceipt(2, "Store B", 50000),
            createReceipt(3, "Store C", 15000),
            createReceipt(4, "Store D", 8000),
            createReceipt(5, "Store E", 12000)
        )
        
        val result = insightEngine.computeInsights(receipts)
        
        val topSpendingInsights = result.filter {
            it.insight.type == InsightType.TOP_SPENDING.value
        }
        assertTrue(topSpendingInsights.isNotEmpty())
        assertTrue(topSpendingInsights.first().insight.description.contains("Store B"))
    }
    
    private fun createReceipt(
        id: Long,
        merchant: String,
        total: Long,
        date: Long = System.currentTimeMillis()
    ): Receipt {
        return Receipt(
            id = id,
            merchant = merchant,
            date = date,
            total = total,
            currency = "INR"
        )
    }
}
