package com.receiptradar.domain.pipeline

import com.receiptradar.domain.model.LineType
import com.receiptradar.domain.model.ParsedLine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantExtractor @Inject constructor() {
    
    private val noisePatterns = listOf(
        Regex("""^\d{2,}[/-]\d{2,}"""),
        Regex("""^\d{1,2}:\d{2}"""),
        Regex("""^[\$€₹]?\d+\.\d{2}$"""),
        Regex("""^#\d+"""),
        Regex("""^tel|^ph:|^fax:""", RegexOption.IGNORE_CASE),
        Regex("""^www\.|^http|^https""", RegexOption.IGNORE_CASE),
        Regex("""@\S+\.\w{2,}"""),
        Regex("""^\d{7,}$"""),
        Regex("""[A-Z]\d[A-Z]\s*\d[A-Z]\d"""),
        Regex("""^\d{5}(-\d{4})?$"""),
        Regex("""^\d{6}$"""),
    )

    fun extract(lines: List<ParsedLine>): ExtractedValue<String> {
        if (lines.isEmpty()) return ExtractedValue("", 0f)
        
        val topLines = lines.take(8)
        
        val candidates = topLines.mapNotNull { line ->
            val score = scoreMerchantCandidate(line)
            if (score > 0) Pair(cleanMerchantName(line.original.text), score) else null
        }
        
        if (candidates.isEmpty()) {
            val firstHeader = topLines.firstOrNull { 
                it.type == LineType.HEADER && it.original.text.length in 3..40 
            }
            if (firstHeader != null) {
                return ExtractedValue(cleanMerchantName(firstHeader.original.text), 0.4f)
            }
            
            val firstLine = lines.firstOrNull { line ->
                val text = line.original.text.trim()
                text.length in 3..40 && text.any { it.isLetter() }
            }
            return if (firstLine != null) {
                ExtractedValue(cleanMerchantName(firstLine.original.text), 0.3f)
            } else {
                ExtractedValue("Unknown Store", 0.1f)
            }
        }
        
        val best = candidates.maxByOrNull { it.second }!!
        return ExtractedValue(best.first, (best.second / 100f).coerceIn(0f, 1f))
    }

    private fun scoreMerchantCandidate(line: ParsedLine): Int {
        val text = line.original.text.trim()
        
        if (noisePatterns.any { it.containsMatchIn(text) }) return 0
        
        val digitRatio = text.count { it.isDigit() }.toFloat() / text.length.coerceAtLeast(1)
        if (digitRatio > 0.5) return 0
        
        if (text.length < 3 || text.length > 50) return 0
        
        if (line.type == LineType.ITEM || line.type == LineType.TOTAL) return 0
        
        if (Regex("""[\$€₹]\s*\d+\.\d{2}|\d+\.\d{2}\s*[\$€₹]""").containsMatchIn(text)) return 0
        
        var score = 40
        
        when (line.original.lineNumber) {
            0 -> score += 35
            1 -> score += 30
            2 -> score += 20
            3, 4 -> score += 10
        }
        
        val letterCount = text.count { it.isLetter() }
        if (letterCount > 0) {
            val upperRatio = text.count { it.isUpperCase() }.toFloat() / letterCount
            if (upperRatio > 0.8) score += 25
            else if (upperRatio > 0.5) score += 10
        }
        
        val letterRatio = letterCount.toFloat() / text.length.coerceAtLeast(1)
        if (letterRatio > 0.7) score += 15
        
        when (text.length) {
            in 5..15 -> score += 15
            in 16..25 -> score += 10
            in 26..35 -> score += 5
        }
        
        val specialChars = text.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        if (specialChars > 3) score -= (specialChars - 3) * 5
        
        if (text.contains(",") && text.first().isDigit()) score -= 20
        
        return score.coerceAtLeast(0)
    }

    private fun cleanMerchantName(text: String): String {
        var name = text.trim()
        name = name.replace(Regex("""\s*(Pvt\.?\s*Ltd\.?|Private\s+Limited|Inc\.?|LLC|LLP|Corp\.?|Co\.?)$""", RegexOption.IGNORE_CASE), "")
        name = name.trim('*', '-', '=', '#', '.', ',', ':', ';')
        name = name.replace(Regex("""\s+"""), " ").trim()
        
        if (name.length > 3 && name == name.uppercase()) {
            name = name.lowercase().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        }
        
        return name.ifEmpty { "Unknown Store" }
    }
}
