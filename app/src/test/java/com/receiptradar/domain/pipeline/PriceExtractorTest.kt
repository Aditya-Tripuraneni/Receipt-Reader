package com.receiptradar.domain.pipeline

import com.receiptradar.domain.model.LineType
import com.receiptradar.domain.model.OcrLine
import com.receiptradar.domain.model.ParsedLine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PriceExtractorTest {
    
    private lateinit var priceExtractor: PriceExtractor
    
    @Before
    fun setup() {
        priceExtractor = PriceExtractor()
    }
    
    @Test
    fun `extractTotal should find explicit total line`() {
        val lines = listOf(
            createParsedLine("Item 1", LineType.ITEM, 10000),
            createParsedLine("Item 2", LineType.ITEM, 15000),
            createParsedLine("Total", LineType.TOTAL, 25000)
        )
        
        val result = priceExtractor.extractTotal(lines)
        
        assertEquals(25000L, result.value)
        assertTrue(result.confidence >= 0.9f)
    }
    
    @Test
    fun `extractTotal should prefer grand total over subtotal`() {
        val lines = listOf(
            createParsedLine("Subtotal", LineType.SUBTOTAL, 20000),
            createParsedLine("Tax", LineType.TAX, 3600),
            createParsedLine("Grand Total", LineType.TOTAL, 23600)
        )
        
        val result = priceExtractor.extractTotal(lines)
        
        assertEquals(23600L, result.value)
    }
    
    @Test
    fun `extractTotal should fall back to item sum when no total`() {
        val lines = listOf(
            createParsedLine("Item 1", LineType.ITEM, 10000),
            createParsedLine("Item 2", LineType.ITEM, 15000)
        )
        
        val result = priceExtractor.extractTotal(lines)
        
        assertEquals(25000L, result.value)
        assertTrue(result.confidence < 0.6f) // Lower confidence for fallback
    }
    
    @Test
    fun `extractTax should sum all tax lines`() {
        val lines = listOf(
            createParsedLine("CGST", LineType.TAX, 1800),
            createParsedLine("SGST", LineType.TAX, 1800)
        )
        
        val result = priceExtractor.extractTax(lines)
        
        assertEquals(3600L, result.value)
    }
    
    private fun createParsedLine(
        text: String,
        type: LineType,
        price: Long?
    ): ParsedLine {
        return ParsedLine(
            original = OcrLine(text, 0.9f, null, 0),
            type = type,
            itemName = if (type == LineType.ITEM) text else null,
            price = price,
            quantity = 1.0,
            confidence = 0.9f
        )
    }
}
