package com.receiptradar.domain.model

import com.receiptradar.data.db.entity.Item
import com.receiptradar.data.db.entity.Receipt

/**
 * Result of processing a receipt image through the OCR and parsing pipeline.
 */
data class ReceiptParseResult(
    /** Parsed receipt (null if parsing completely failed) */
    val receipt: Receipt?,
    
    /** Parsed line items */
    val items: List<Item>,
    
    /** Errors encountered during parsing */
    val errors: List<String>,
    
    /** Warnings (non-critical issues) */
    val warnings: List<String>,
    
    /** Confidence scores for each extracted entity */
    val confidences: Map<String, Float>
) {
    val isSuccessful: Boolean get() = receipt != null && errors.isEmpty()
    
    val overallConfidence: Float get() = 
        if (confidences.isEmpty()) 0f 
        else confidences.values.average().toFloat()
    
    companion object {
        fun failure(error: String) = ReceiptParseResult(
            receipt = null,
            items = emptyList(),
            errors = listOf(error),
            warnings = emptyList(),
            confidences = emptyMap()
        )
    }
}

/**
 * Intermediate result from OCR processing.
 */
data class OcrResult(
    /** Raw text from OCR */
    val rawText: String,
    
    /** Lines with bounding boxes and confidence */
    val lines: List<OcrLine>,
    
    /** Overall OCR confidence */
    val confidence: Float
)

/**
 * A single line from OCR with metadata.
 */
data class OcrLine(
    val text: String,
    val confidence: Float,
    val boundingBox: BoundingBox?,
    val lineNumber: Int
)

/**
 * Bounding box for OCR text.
 */
data class BoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val centerY: Int get() = (top + bottom) / 2
    val height: Int get() = bottom - top
}

/**
 * Type of line detected in receipt.
 */
enum class LineType {
    HEADER,
    ITEM,
    TOTAL,
    SUBTOTAL,
    TAX,
    FOOTER,
    DATE,
    UNKNOWN
}

/**
 * Parsed line with type classification.
 */
data class ParsedLine(
    val original: OcrLine,
    val type: LineType,
    val itemName: String? = null,
    val price: Long? = null,
    val quantity: Double? = null,
    val confidence: Float = 0f
)
