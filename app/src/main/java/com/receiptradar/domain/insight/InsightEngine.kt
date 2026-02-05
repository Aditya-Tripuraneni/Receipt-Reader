package com.receiptradar.domain.insight

import com.receiptradar.data.db.entity.Insight
import com.receiptradar.data.db.entity.InsightType
import com.receiptradar.data.db.entity.Receipt
import com.google.gson.Gson
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine for computing spending insights from receipts.
 */
@Singleton
class InsightEngine @Inject constructor() {
    
    private val gson = Gson()
    
    /**
     * Compute all insights from receipts.
     */
    fun computeInsights(receipts: List<Receipt>): List<InsightWithReceiptIds> {
        if (receipts.isEmpty()) return emptyList()
        
        val insights = mutableListOf<InsightWithReceiptIds>()
        
        // Compute different types of insights
        insights.addAll(computeCategoryBreakdown(receipts))
        insights.addAll(computeRecurringPatterns(receipts))
        insights.addAll(computeTrends(receipts))
        insights.addAll(computeWeekdayAnalysis(receipts))
        insights.addAll(computeMerchantSummary(receipts))
        insights.addAll(computeTopSpending(receipts))
        
        return insights.sortedByDescending { it.insight.confidence }
    }
    
    /**
     * Compute category spending breakdown.
     */
    private fun computeCategoryBreakdown(receipts: List<Receipt>): List<InsightWithReceiptIds> {
        // Group by month for this month vs last month comparison
        val now = Calendar.getInstance()
        val thisMonth = receipts.filter { receipt ->
            val cal = Calendar.getInstance().apply { timeInMillis = receipt.date }
            cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        }
        
        if (thisMonth.isEmpty()) return emptyList()
        
        // Calculate total spending this month
        val totalThisMonth = thisMonth.sumOf { it.total }
        
        // Group by merchant as a proxy for category
        val byMerchant = thisMonth.groupBy { it.merchant }
        val topMerchant = byMerchant.maxByOrNull { it.value.sumOf { r -> r.total } }
        
        if (topMerchant != null) {
            val merchantTotal = topMerchant.value.sumOf { it.total }
            val percentage = (merchantTotal * 100 / totalThisMonth).toInt()
            
            if (percentage >= 10) {
                val description = "${topMerchant.key} accounts for $percentage% of your spending this month"
                
                return listOf(
                    InsightWithReceiptIds(
                        insight = Insight(
                            type = InsightType.CATEGORY_BREAKDOWN.value,
                            description = description,
                            confidence = 0.85f,
                            payload = gson.toJson(mapOf(
                                "merchant" to topMerchant.key,
                                "percentage" to percentage,
                                "amount" to merchantTotal
                            ))
                        ),
                        receiptIds = topMerchant.value.map { it.id }
                    )
                )
            }
        }
        
        return emptyList()
    }
    
    /**
     * Detect recurring spending patterns (subscriptions).
     */
    private fun computeRecurringPatterns(receipts: List<Receipt>): List<InsightWithReceiptIds> {
        val insights = mutableListOf<InsightWithReceiptIds>()
        
        // Group by merchant
        val byMerchant = receipts.groupBy { it.merchant.lowercase() }
        
        for ((merchant, merchantReceipts) in byMerchant) {
            if (merchantReceipts.size < 3) continue
            
            // Sort by date
            val sorted = merchantReceipts.sortedBy { it.date }
            
            // Calculate intervals between purchases
            val intervals = sorted.zipWithNext { a, b ->
                (b.date - a.date) / (24 * 60 * 60 * 1000) // Days
            }
            
            if (intervals.isEmpty()) continue
            
            // Check for consistent interval (monthly ~30 days)
            val avgInterval = intervals.average()
            val variance = intervals.map { (it - avgInterval) * (it - avgInterval) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            
            // If standard deviation is low and interval is around 28-32 days
            if (stdDev < 5 && avgInterval in 25.0..35.0) {
                val avgAmount = merchantReceipts.map { it.total }.average().toLong()
                val displayAmount = formatAmount(avgAmount)
                
                insights.add(
                    InsightWithReceiptIds(
                        insight = Insight(
                            type = InsightType.RECURRING.value,
                            description = "Likely recurring charge: ${merchantReceipts.first().merchant} every ~${avgInterval.toInt()} days ($displayAmount)",
                            confidence = 0.9f,
                            payload = gson.toJson(mapOf(
                                "merchant" to merchant,
                                "intervalDays" to avgInterval.toInt(),
                                "amount" to avgAmount
                            ))
                        ),
                        receiptIds = merchantReceipts.map { it.id }
                    )
                )
            }
            
            // Check for weekly patterns
            if (stdDev < 2 && avgInterval in 5.0..9.0) {
                val count = merchantReceipts.size
                val displayMerchant = merchantReceipts.first().merchant
                
                insights.add(
                    InsightWithReceiptIds(
                        insight = Insight(
                            type = InsightType.RECURRING.value,
                            description = "You visit $displayMerchant about weekly ($count visits recorded)",
                            confidence = 0.85f,
                            payload = gson.toJson(mapOf(
                                "merchant" to merchant,
                                "intervalDays" to avgInterval.toInt(),
                                "count" to count
                            ))
                        ),
                        receiptIds = merchantReceipts.map { it.id }
                    )
                )
            }
        }
        
        return insights
    }
    
    /**
     * Compute spending trends (month over month).
     */
    private fun computeTrends(receipts: List<Receipt>): List<InsightWithReceiptIds> {
        val now = Calendar.getInstance()
        
        // This month's receipts
        val thisMonth = receipts.filter { receipt ->
            val cal = Calendar.getInstance().apply { timeInMillis = receipt.date }
            cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        }
        
        // Last month's receipts
        val lastMonth = now.clone() as Calendar
        lastMonth.add(Calendar.MONTH, -1)
        
        val lastMonthReceipts = receipts.filter { receipt ->
            val cal = Calendar.getInstance().apply { timeInMillis = receipt.date }
            cal.get(Calendar.YEAR) == lastMonth.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == lastMonth.get(Calendar.MONTH)
        }
        
        if (thisMonth.isEmpty() || lastMonthReceipts.isEmpty()) return emptyList()
        
        val thisMonthTotal = thisMonth.sumOf { it.total }
        val lastMonthTotal = lastMonthReceipts.sumOf { it.total }
        
        if (lastMonthTotal == 0L) return emptyList()
        
        val changePercent = ((thisMonthTotal - lastMonthTotal) * 100 / lastMonthTotal).toInt()
        
        if (kotlin.math.abs(changePercent) >= 10) {
            val direction = if (changePercent > 0) "up" else "down"
            val absChange = kotlin.math.abs(changePercent)
            
            return listOf(
                InsightWithReceiptIds(
                    insight = Insight(
                        type = InsightType.TREND.value,
                        description = "Your spending is $direction $absChange% compared to last month",
                        confidence = 0.8f,
                        payload = gson.toJson(mapOf(
                            "changePercent" to changePercent,
                            "thisMonth" to thisMonthTotal,
                            "lastMonth" to lastMonthTotal
                        ))
                    ),
                    receiptIds = thisMonth.map { it.id }
                )
            )
        }
        
        return emptyList()
    }
    
    /**
     * Analyze spending by weekday.
     */
    private fun computeWeekdayAnalysis(receipts: List<Receipt>): List<InsightWithReceiptIds> {
        if (receipts.size < 7) return emptyList()
        
        val byWeekday = receipts.groupBy { receipt ->
            Calendar.getInstance().apply { timeInMillis = receipt.date }.get(Calendar.DAY_OF_WEEK)
        }
        
        val weekdayTotals = byWeekday.mapValues { (_, receipts) ->
            receipts.sumOf { it.total }
        }
        
        val maxWeekday = weekdayTotals.maxByOrNull { it.value } ?: return emptyList()
        val weekdayName = getWeekdayName(maxWeekday.key)
        val avgSpend = formatAmount(maxWeekday.value / byWeekday[maxWeekday.key]!!.size)
        
        return listOf(
            InsightWithReceiptIds(
                insight = Insight(
                    type = InsightType.WEEKDAY.value,
                    description = "Your highest spending day is $weekdayName (avg $avgSpend per receipt)",
                    confidence = 0.75f,
                    payload = gson.toJson(mapOf(
                        "weekday" to maxWeekday.key,
                        "weekdayName" to weekdayName,
                        "total" to maxWeekday.value
                    ))
                ),
                receiptIds = byWeekday[maxWeekday.key]?.map { it.id } ?: emptyList()
            )
        )
    }
    
    /**
     * Compute top merchant summary.
     */
    private fun computeMerchantSummary(receipts: List<Receipt>): List<InsightWithReceiptIds> {
        val byMerchant = receipts.groupBy { it.merchant }
        
        if (byMerchant.size < 3) return emptyList()
        
        val topByVisits = byMerchant.maxByOrNull { it.value.size } ?: return emptyList()
        
        if (topByVisits.value.size >= 3) {
            return listOf(
                InsightWithReceiptIds(
                    insight = Insight(
                        type = InsightType.MERCHANT_SUMMARY.value,
                        description = "Most visited: ${topByVisits.key} (${topByVisits.value.size} visits)",
                        confidence = 0.8f,
                        payload = gson.toJson(mapOf(
                            "merchant" to topByVisits.key,
                            "visits" to topByVisits.value.size
                        ))
                    ),
                    receiptIds = topByVisits.value.map { it.id }
                )
            )
        }
        
        return emptyList()
    }
    
    /**
     * Compute top spending receipts.
     */
    private fun computeTopSpending(receipts: List<Receipt>): List<InsightWithReceiptIds> {
        if (receipts.size < 5) return emptyList()
        
        val sorted = receipts.sortedByDescending { it.total }
        val topReceipt = sorted.first()
        
        val displayAmount = formatAmount(topReceipt.total)
        
        return listOf(
            InsightWithReceiptIds(
                insight = Insight(
                    type = InsightType.TOP_SPENDING.value,
                    description = "Largest purchase: ${topReceipt.merchant} - $displayAmount",
                    confidence = 0.95f,
                    payload = gson.toJson(mapOf(
                        "merchant" to topReceipt.merchant,
                        "amount" to topReceipt.total
                    ))
                ),
                receiptIds = listOf(topReceipt.id)
            )
        )
    }
    
    private fun formatAmount(amountInPaise: Long): String {
        val rupees = amountInPaise / 100.0
        return "₹${String.format("%.2f", rupees)}"
    }
    
    private fun getWeekdayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }
}

/**
 * Helper class to associate insight with related receipt IDs.
 */
data class InsightWithReceiptIds(
    val insight: Insight,
    val receiptIds: List<Long>
)
