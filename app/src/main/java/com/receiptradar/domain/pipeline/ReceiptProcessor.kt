package com.receiptradar.domain.pipeline

import android.graphics.Bitmap
import android.net.Uri
import com.receiptradar.data.db.entity.Item
import com.receiptradar.data.db.entity.Receipt
import com.receiptradar.domain.model.LineType
import com.receiptradar.domain.model.ReceiptParseResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptProcessor @Inject constructor(
    private val imagePreprocessor: ImagePreprocessor,
    private val ocrEngine: OcrEngine,
    private val lineParser: LineParser,
    private val priceExtractor: PriceExtractor,
    private val dateExtractor: DateExtractor,
    private val merchantExtractor: MerchantExtractor,
    private val categorizer: Categorizer,
    private val currencyDetector: CurrencyDetector
) {

    suspend fun process(
        image: Bitmap, 
        imagePath: String? = null,
        imageUri: Uri? = null
    ): ReceiptParseResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val confidences = mutableMapOf<String, Float>()
        
        try {
            val processedImage = imagePreprocessor.preprocess(image, imageUri)
            val ocrResult = ocrEngine.recognize(processedImage)
            
            if (ocrResult.lines.isEmpty()) {
                return ReceiptParseResult.failure("No text detected in image")
            }
            
            confidences["ocr"] = ocrResult.confidence
            
            val parsedLines = lineParser.parseLines(ocrResult.lines)
            
            val merchant = merchantExtractor.extract(parsedLines)
            confidences["merchant"] = merchant.confidence
            if (merchant.value.isBlank()) warnings.add("Could not detect merchant name")
            
            val date = dateExtractor.extract(parsedLines)
            confidences["date"] = date.confidence
            if (date.value == 0L) warnings.add("Could not detect date, using current time")
            
            val total = priceExtractor.extractTotal(parsedLines)
            confidences["total"] = total.confidence
            
            val currency = currencyDetector.detect(ocrResult.rawText)
            confidences["currency"] = currency.confidence

            val itemLines = parsedLines.filter { it.type == LineType.ITEM }
            val items = itemLines.mapIndexedNotNull { index, line ->
                if (line.itemName != null && line.price != null) {
                    Item(
                        receiptId = 0,
                        name = line.itemName,
                        price = line.price,
                        quantity = line.quantity ?: 1.0,
                        sortOrder = index,
                        confidence = line.confidence
                    )
                } else null
            }
            
            val itemsSum = items.sumOf { (it.price * it.quantity).toLong() }
            if (total.value > 0 && items.isNotEmpty()) {
                val discrepancy = kotlin.math.abs(itemsSum - total.value)
                val tolerance = total.value * 0.15
                if (discrepancy > tolerance) {
                    warnings.add("Item total ($itemsSum) differs from receipt total (${total.value})")
                }
            }
            
            val receipt = Receipt(
                merchant = merchant.value.ifBlank { "Unknown Merchant" },
                date = if (date.value > 0) date.value else System.currentTimeMillis(),
                total = if (total.value > 0) total.value else itemsSum,
                currency = currency.value,
                rawText = ocrResult.rawText,
                imagePath = imagePath,
                confidence = confidences.values.average().toFloat()
            )
            
            val categorizedItems = items.map { item ->
                val categoryId = categorizer.categorize(item.name, merchant.value)
                item.copy(categoryId = categoryId)
            }
            
            return ReceiptParseResult(
                receipt = receipt,
                items = categorizedItems,
                errors = errors,
                warnings = warnings,
                confidences = confidences
            )
            
        } catch (e: Exception) {
            errors.add("Processing failed: ${e.message}")
            return ReceiptParseResult(
                receipt = null,
                items = emptyList(),
                errors = errors,
                warnings = warnings,
                confidences = confidences
            )
        }
    }
}

data class ExtractedValue<T>(
    val value: T,
    val confidence: Float
)
