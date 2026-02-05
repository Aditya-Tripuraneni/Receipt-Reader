package com.receiptradar.domain.pipeline

import com.receiptradar.domain.model.LineType
import com.receiptradar.domain.model.ParsedLine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateExtractor @Inject constructor() {
    
    private val dateFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.US),
        SimpleDateFormat("dd-MM-yyyy", Locale.US),
        SimpleDateFormat("MM/dd/yyyy", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("dd/MM/yy", Locale.US),
        SimpleDateFormat("dd-MM-yy", Locale.US),
        SimpleDateFormat("dd MMM yyyy", Locale.US),
        SimpleDateFormat("MMM dd, yyyy", Locale.US),
        SimpleDateFormat("dd.MM.yyyy", Locale.US)
    ).onEach { it.isLenient = false }
    
    private val datePatterns = listOf(
        Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})"""),
        Regex("""(\d{4})[/-](\d{1,2})[/-](\d{1,2})"""),
        Regex("""(\d{1,2})\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\w*\s+(\d{2,4})""", RegexOption.IGNORE_CASE),
        Regex("""(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\w*\s+(\d{1,2}),?\s+(\d{2,4})""", RegexOption.IGNORE_CASE)
    )

    fun extract(lines: List<ParsedLine>): ExtractedValue<Long> {
        val dateLines = lines.filter { it.type == LineType.DATE }
        
        for (line in dateLines) {
            val date = parseDate(line.original.text)
            if (date != null) return ExtractedValue(date.time, 0.9f)
        }
        
        val topHalf = lines.take(lines.size / 2 + 3)
        for (line in topHalf) {
            val date = parseDate(line.original.text)
            if (date != null) return ExtractedValue(date.time, 0.8f)
        }
        
        val bottomQuarter = lines.takeLast(lines.size / 4 + 2)
        for (line in bottomQuarter) {
            val date = parseDate(line.original.text)
            if (date != null) return ExtractedValue(date.time, 0.7f)
        }
        
        return ExtractedValue(System.currentTimeMillis(), 0.1f)
    }

    private fun parseDate(text: String): Date? {
        for (pattern in datePatterns) {
            val match = pattern.find(text) ?: continue
            val dateStr = match.value
            
            for (format in dateFormats) {
                try {
                    val date = format.parse(dateStr)
                    if (date != null && isReasonableDate(date)) return date
                } catch (_: Exception) {}
            }
        }
        
        for (format in dateFormats) {
            try {
                val date = format.parse(text.trim())
                if (date != null && isReasonableDate(date)) return date
            } catch (_: Exception) {}
        }
        
        return null
    }

    private fun isReasonableDate(date: Date): Boolean {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply { time = date }
        
        val year = calendar.get(Calendar.YEAR)
        val currentYear = now.get(Calendar.YEAR)
        
        if (year < currentYear - 2 || year > currentYear + 1) return false
        
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        return !date.after(tomorrow.time)
    }
}
