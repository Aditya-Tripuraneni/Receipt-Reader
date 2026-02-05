package com.receiptradar.domain.pipeline

import com.receiptradar.domain.model.BoundingBox
import com.receiptradar.domain.model.LineType
import com.receiptradar.domain.model.OcrLine
import com.receiptradar.domain.model.ParsedLine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineParser @Inject constructor() {
    
    private val priceRegex = Regex(
        """(?:₹|Rs\.?|INR|USD|CAD|C?\$|€)?\s*(\d{1,6}[.,]\d{2})(?:\s*[A-Z])?(?:\s*(?:₹|Rs\.?|INR|USD|CAD|\$|€))?""",
        RegexOption.IGNORE_CASE
    )
    
    private val quantityRegex = Regex(
        """(?:(\d+)\s*[xX×]|[xX×]\s*(\d+)|qty\s*:?\s*(\d+)|(\d+)\s*(?:nos?|pcs?|units?))""",
        RegexOption.IGNORE_CASE
    )
    
    // Keywords to identify as TOTAL lines (fuzzy matched)
    private val totalKeywords = listOf(
        "TOTAL", "GRAND TOTAL", "AMOUNT DUE", "BALANCE DUE", "TOTAL DUE"
    )
    
    // Keywords to EXCLUDE from items (fuzzy matched - NO REGEX!)
    private val excludeKeywords = listOf(
        "SUBTOTAL", "SUB-TOTAL", "SUB TOTAL",
        "TAX", "HST", "GST", "PST", "VAT",
        "TIP", "GRATUITY", "SERVICE CHARGE",
        "DISCOUNT", "SAVINGS", "PROMO",
        "CHANGE", "CASH", "CREDIT", "DEBIT",
        "VISA", "MASTERCARD", "AMEX", "CARD",
        "PAYMENT", "TENDERED", "PAID",
        "BALANCE", "REFUND",
        "MEMBER", "REWARDS", "POINTS"
    )

    fun parseLines(lines: List<OcrLine>): List<ParsedLine> {
        val spatiallyMerged = mergeBySpatialProximity(lines)
        val textMerged = mergeAdjacentLines(spatiallyMerged)
        val parsed = textMerged.map { parseLine(it) }
        return identifyTotal(parsed)
    }

    private fun mergeBySpatialProximity(lines: List<OcrLine>): List<OcrLine> {
        if (lines.size < 2) return lines
        
        val sortedByY = lines.sortedBy { it.boundingBox?.top ?: 0 }
        val result = mutableListOf<OcrLine>()
        val used = mutableSetOf<Int>()
        
        for (i in sortedByY.indices) {
            if (i in used) continue
            
            val current = sortedByY[i]
            val currentBox = current.boundingBox
            
            if (currentBox == null) {
                result.add(current)
                continue
            }
            
            val sameLine = mutableListOf(current)
            
            for (j in i + 1 until sortedByY.size) {
                if (j in used) continue
                
                val other = sortedByY[j]
                val otherBox = other.boundingBox ?: continue
                
                if (areOnSameLine(currentBox, otherBox)) {
                    sameLine.add(other)
                    used.add(j)
                }
            }
            
            if (sameLine.size > 1) {
                val sorted = sameLine.sortedBy { it.boundingBox?.left ?: 0 }
                val mergedText = sorted.joinToString(" ") { it.text }
                val mergedBox = BoundingBox(
                    left = sorted.minOf { it.boundingBox?.left ?: 0 },
                    top = sorted.minOf { it.boundingBox?.top ?: 0 },
                    right = sorted.maxOf { it.boundingBox?.right ?: 0 },
                    bottom = sorted.maxOf { it.boundingBox?.bottom ?: 0 }
                )
                result.add(current.copy(text = mergedText, boundingBox = mergedBox))
                android.util.Log.d("LineParser", "Spatial merge: ${sameLine.map { it.text }} -> '$mergedText'")
            } else {
                result.add(current)
            }
        }
        
        return result.sortedBy { it.boundingBox?.top ?: 0 }
    }

    private fun areOnSameLine(box1: BoundingBox, box2: BoundingBox): Boolean {
        val height1 = box1.bottom - box1.top
        val height2 = box2.bottom - box2.top
        val avgHeight = (height1 + height2) / 2
        
        val center1 = (box1.top + box1.bottom) / 2
        val center2 = (box2.top + box2.bottom) / 2
        
        return kotlin.math.abs(center1 - center2) < avgHeight * 0.6
    }

    private fun mergeAdjacentLines(lines: List<OcrLine>): List<OcrLine> {
        if (lines.size < 2) return lines
        
        val result = mutableListOf<OcrLine>()
        var i = 0
        
        while (i < lines.size) {
            val current = lines[i]
            val next = if (i + 1 < lines.size) lines[i + 1] else null
            
            val currentHasPrice = priceRegex.containsMatchIn(current.text)
            val currentLooksLikeItem = current.text.length >= 5 && 
                current.text.any { it.isLetter() } &&
                !isSeparatorLine(current.text)
            
            val nextIsJustPrice = next != null && 
                priceRegex.containsMatchIn(next.text) &&
                next.text.replace(Regex("""[\d\s.,\-+H]"""), "").length < 3
            
            if (!currentHasPrice && currentLooksLikeItem && nextIsJustPrice && next != null) {
                val mergedText = "${current.text} ${next.text}"
                android.util.Log.d("LineParser", "Text merge: '${current.text}' + '${next.text}'")
                result.add(current.copy(text = mergedText))
                i += 2
            } else {
                result.add(current)
                i += 1
            }
        }
        
        return result
    }

    /**
     * Calculate Levenshtein distance between two strings (case-insensitive).
     * Returns the minimum number of single-character edits needed to change s1 into s2.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val str1 = s1.uppercase()
        val str2 = s2.uppercase()
        
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[str1.length][str2.length]
    }
    
    /**
     * Check if text contains a word that fuzzy-matches any keyword.
     * Uses Levenshtein distance with adaptive threshold based on word length.
     */
    private fun containsFuzzyMatch(text: String, keywords: List<String>): Boolean {
        val words = text.uppercase()
            .split(Regex("\\s+|(?<=[a-z])(?=[A-Z])")) // Split on space or camelCase
            .filter { it.length >= 3 } // Ignore very short words
        
        for (word in words) {
            for (keyword in keywords) {
                val distance = levenshteinDistance(word, keyword)
                val threshold = when (keyword.length) {
                    in 0..4 -> 1    // Short words (TAX, HST): max 1 edit
                    in 5..7 -> 2    // Medium words (TOTAL, CREDIT): max 2 edits
                    else -> 3       // Long words (SUBTOTAL): max 3 edits
                }
                
                if (distance <= threshold) {
                    android.util.Log.d("LineParser", 
                        "Fuzzy match: '$word' ≈ '$keyword' (distance=$distance, threshold=$threshold)")
                    return true
                }
            }
        }
        
        return false
    }

    private fun parseLine(line: OcrLine): ParsedLine {
        val text = line.text.trim()
        
        if (isNoiseLine(text)) {
            return ParsedLine(line, LineType.UNKNOWN, confidence = 0.1f)
        }
        
        val price = extractPrice(text)
        
        if (price != null) {
            val isTotal = containsFuzzyMatch(text, totalKeywords)
            val isExcluded = containsFuzzyMatch(text, excludeKeywords)
            
            when {
                isTotal -> {
                    android.util.Log.d("LineParser", "TOTAL: '$text' @ $price")
                    return ParsedLine(line, LineType.TOTAL, price = price, confidence = 0.9f)
                }
                isExcluded -> {
                    android.util.Log.d("LineParser", "EXCLUDED: '$text' (fuzzy matched exclude keyword)")
                    return ParsedLine(line, LineType.UNKNOWN, confidence = 0.2f)
                }
                else -> {
                    val itemName = extractItemName(text)
                    val quantity = extractQuantity(text)
                    
                    if (itemName.length >= 2) {
                        android.util.Log.d("LineParser", "ITEM: '$itemName' @ $price")
                        return ParsedLine(
                            line, LineType.ITEM,
                            itemName = itemName,
                            price = price,
                            quantity = quantity,
                            confidence = line.confidence
                        )
                    }
                }
            }
        }
        
        return ParsedLine(line, LineType.UNKNOWN, confidence = 0.2f)
    }

    private fun identifyTotal(lines: List<ParsedLine>): List<ParsedLine> {
        val totalCandidates = lines.filter { it.type == LineType.TOTAL && it.price != null }
        if (totalCandidates.isEmpty()) return lines
        
        val bestTotal = totalCandidates.maxByOrNull { it.price ?: 0L }
        
        return lines.map { line ->
            if (line.type == LineType.TOTAL && line != bestTotal) {
                line.copy(type = LineType.UNKNOWN)
            } else {
                line
            }
        }
    }

    private fun isNoiseLine(text: String): Boolean {
        if (text.length < 3) return true
        if (isSeparatorLine(text)) return true
        
        val stripped = text.replace(Regex("""\s"""), "")
        if (stripped.length > 12 && stripped.count { it.isDigit() } > stripped.length * 0.85) return true
        
        return false
    }

    private fun isSeparatorLine(text: String): Boolean {
        val separatorChars = text.count { it in "*-=_#" }
        return separatorChars > text.length * 0.3 && separatorChars >= 4
    }

    private fun extractPrice(text: String): Long? {
        val matches = priceRegex.findAll(text).toList()
        if (matches.isEmpty()) return null
        
        val match = matches.lastOrNull() ?: return null
        val priceStr = match.groupValues[1].replace(",", ".").trim()
        
        return try {
            val price = priceStr.toDouble()
            if (price > 10000) null else (price * 100).toLong()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun extractItemName(text: String): String {
        var name = text
        name = priceRegex.replace(name, " ")
        name = quantityRegex.replace(name, " ")
        name = name.replace(Regex("""[₹$€@#*]"""), " ")
        name = name.replace(Regex("""^\d{6,}\s*"""), "")
        name = name.replace(Regex("""\s+"""), " ")
        
        return name.trim()
            .take(60)
            .trimEnd('.', '-', ':', '*')
            .trim()
    }

    private fun extractQuantity(text: String): Double? {
        val match = quantityRegex.find(text) ?: return null
        val qty = match.groupValues.drop(1).firstOrNull { it.isNotBlank() }
        return qty?.toDoubleOrNull()
    }
}
