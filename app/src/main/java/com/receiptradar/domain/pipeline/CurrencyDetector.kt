package com.receiptradar.domain.pipeline

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyDetector @Inject constructor() {
    
    private val currencyPatterns = listOf(
        // Explicit currency symbols (highest priority)
        CurrencyPattern("CAD", listOf("CAD", "C$", "CA$"), 0.95f),
        CurrencyPattern("USD", listOf("USD", "US$"), 0.95f),
        CurrencyPattern("INR", listOf("INR", "₹", "Rs.", "Rs"), 0.95f),
        CurrencyPattern("EUR", listOf("EUR", "€"), 0.95f),
        CurrencyPattern("GBP", listOf("GBP", "£"), 0.95f),
        CurrencyPattern("AUD", listOf("AUD", "A$", "AU$"), 0.95f),
        
        // Regional keywords (medium-high priority)
        CurrencyPattern("CAD", listOf("INTERAC", "CANADA", "CANADIAN"), 0.85f),
        CurrencyPattern("USD", listOf("USA", "UNITED STATES"), 0.85f),
        CurrencyPattern("INR", listOf("INDIA", "CGST", "SGST", "GSTIN"), 0.9f),
        CurrencyPattern("EUR", listOf("EURO", "GERMANY", "FRANCE", "ITALY"), 0.85f),
        CurrencyPattern("GBP", listOf("UNITED KINGDOM", "ENGLAND"), 0.85f),
        
        // Canadian postal code pattern (high priority)
        CurrencyPattern("CAD", listOf("""[A-Z]\d[A-Z]\s*\d[A-Z]\d"""), 0.9f, isRegex = true),
        
        // Canadian province codes (medium priority)
        CurrencyPattern("CAD", listOf("ON ", "BC ", "AB ", "QC ", "MB ", "SK "), 0.7f),
        
        // Indian PIN code (6 consecutive digits)
        CurrencyPattern("INR", listOf("""\b\d{6}\b"""), 0.6f, isRegex = true),
        
        // Bare $ is ambiguous - weak signal, defaulting slightly toward USD
        CurrencyPattern("USD", listOf("$"), 0.3f),
    )

    fun detect(rawText: String): ExtractedValue<String> {
        if (rawText.isBlank()) return ExtractedValue("USD", 0.1f)
        
        val upperText = rawText.uppercase()
        val scores = mutableMapOf<String, Float>()
        
        for (pattern in currencyPatterns) {
            for (keyword in pattern.keywords) {
                val found = if (pattern.isRegex) {
                    Regex(keyword, RegexOption.IGNORE_CASE).containsMatchIn(upperText)
                } else {
                    upperText.contains(keyword.uppercase())
                }
                
                if (found) {
                    android.util.Log.d("CurrencyDetector", "Match: '$keyword' -> ${pattern.currency} (${pattern.confidence})")
                    val current = scores.getOrDefault(pattern.currency, 0f)
                    scores[pattern.currency] = maxOf(current, pattern.confidence)
                }
            }
        }
        
        android.util.Log.d("CurrencyDetector", "Scores: $scores")
        
        val best = scores.maxByOrNull { it.value }
        return if (best != null && best.value > 0.5f) {
            ExtractedValue(best.key, best.value)
        } else {
            ExtractedValue("USD", 0.3f)
        }
    }

    fun getSymbol(currencyCode: String): String = when (currencyCode) {
        "INR" -> "₹"
        "CAD" -> "C$"
        "EUR" -> "€"
        "GBP" -> "£"
        "AUD" -> "A$"
        else -> "$"
    }

    private data class CurrencyPattern(
        val currency: String,
        val keywords: List<String>,
        val confidence: Float,
        val isRegex: Boolean = false
    )
}
