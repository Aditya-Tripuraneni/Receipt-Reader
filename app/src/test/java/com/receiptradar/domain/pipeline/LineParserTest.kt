package com.receiptradar.domain.pipeline

import com.receiptradar.domain.model.BoundingBox
import com.receiptradar.domain.model.LineType
import com.receiptradar.domain.model.OcrLine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LineParserTest {
    
    private lateinit var lineParser: LineParser
    
    @Before
    fun setup() {
        lineParser = LineParser()
    }
    
    @Test
    fun `parseLines should identify item lines with prices`() {
        val lines = listOf(
            OcrLine("Coffee Latte ₹150.00", 0.9f, null, 0),
            OcrLine("Sandwich ₹120.00", 0.9f, null, 1)
        )
        
        val result = lineParser.parseLines(lines)
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.type == LineType.ITEM })
        assertEquals("Coffee Latte", result[0].itemName?.trim())
        assertEquals(15000L, result[0].price) // 150.00 * 100
    }
    
    @Test
    fun `parseLines should identify total lines`() {
        val lines = listOf(
            OcrLine("Total ₹270.00", 0.9f, null, 0)
        )
        
        val result = lineParser.parseLines(lines)
        
        assertEquals(1, result.size)
        assertEquals(LineType.TOTAL, result[0].type)
        assertEquals(27000L, result[0].price)
    }
    
    @Test
    fun `parseLines should identify subtotal lines`() {
        val lines = listOf(
            OcrLine("Subtotal ₹250.00", 0.9f, null, 0)
        )
        
        val result = lineParser.parseLines(lines)
        
        assertEquals(LineType.SUBTOTAL, result[0].type)
    }
    
    @Test
    fun `parseLines should identify tax lines`() {
        val lines = listOf(
            OcrLine("GST 18% ₹45.00", 0.9f, null, 0),
            OcrLine("CGST ₹22.50", 0.9f, null, 1)
        )
        
        val result = lineParser.parseLines(lines)
        
        assertTrue(result.all { it.type == LineType.TAX })
    }
    
    @Test
    fun `parseLines should handle various price formats`() {
        val testCases = listOf(
            "Item Rs.100.00" to 10000L,
            "Item Rs 50" to 5000L,
            "Item ₹75.50" to 7550L,
            "Item 200.00" to 20000L,
            "Item $25.00" to 2500L
        )
        
        for ((input, expectedPrice) in testCases) {
            val lines = listOf(OcrLine(input, 0.9f, null, 0))
            val result = lineParser.parseLines(lines)
            
            assertEquals(
                "Failed for input: $input",
                expectedPrice,
                result[0].price
            )
        }
    }
    
    @Test
    fun `parseLines should extract quantity`() {
        val lines = listOf(
            OcrLine("Coffee 2x ₹150.00", 0.9f, null, 0),
            OcrLine("Tea x3 ₹90.00", 0.9f, null, 1)
        )
        
        val result = lineParser.parseLines(lines)
        
        assertEquals(2.0, result[0].quantity)
        assertEquals(3.0, result[1].quantity)
    }
    
    @Test
    fun `parseLines should identify header lines`() {
        val lines = listOf(
            OcrLine("TAX INVOICE", 0.9f, null, 0),
            OcrLine("RECEIPT", 0.9f, null, 1)
        )
        
        val result = lineParser.parseLines(lines)
        
        assertTrue(result.all { it.type == LineType.HEADER })
    }
    
    @Test
    fun `parseLines should identify footer lines`() {
        val lines = listOf(
            OcrLine("Thank you for visiting!", 0.9f, null, 0),
            OcrLine("Visit Again", 0.9f, null, 1)
        )
        
        val result = lineParser.parseLines(lines)
        
        assertTrue(result.all { it.type == LineType.FOOTER })
    }
}
