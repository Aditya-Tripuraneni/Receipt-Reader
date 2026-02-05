package com.receiptradar.domain.pipeline

import com.receiptradar.domain.model.LineType
import com.receiptradar.domain.model.ParsedLine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceExtractor @Inject constructor() {

    fun extractTotal(lines: List<ParsedLine>): ExtractedValue<Long> {
        val totalLines = lines.filter { it.type == LineType.TOTAL && it.price != null }
        
        if (totalLines.isNotEmpty()) {
            val maxTotal = totalLines.maxByOrNull { it.price ?: 0 }
            if (maxTotal?.price != null) {
                return ExtractedValue(maxTotal.price, 0.95f)
            }
        }
        
        val bottomThird = lines.takeLast(lines.size / 3 + 1)
        val highestInBottom = bottomThird
            .filter { it.price != null }
            .maxByOrNull { it.price ?: 0 }
        
        if (highestInBottom?.price != null) {
            return ExtractedValue(highestInBottom.price, 0.7f)
        }
        
        val itemSum = lines
            .filter { it.type == LineType.ITEM && it.price != null }
            .sumOf { (it.price ?: 0) * (it.quantity ?: 1.0).toLong() }
        
        if (itemSum > 0) {
            return ExtractedValue(itemSum, 0.5f)
        }
        
        return ExtractedValue(0L, 0f)
    }
}
