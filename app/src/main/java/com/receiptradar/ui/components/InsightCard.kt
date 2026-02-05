package com.receiptradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptradar.data.db.entity.Insight
import com.receiptradar.data.db.entity.InsightType
import com.receiptradar.ui.theme.*

@Composable
fun InsightCard(
    insight: Insight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getInsightIconAndColor(insight.type)
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Insight text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getInsightTypeLabel(insight.type),
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Confidence badge
            if (insight.confidence >= 0.8f) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "High confidence",
                    modifier = Modifier.size(20.dp),
                    tint = ConfidenceHigh
                )
            }
        }
    }
}

private fun getInsightIconAndColor(type: String): Pair<ImageVector, androidx.compose.ui.graphics.Color> {
    return when (InsightType.fromValue(type)) {
        InsightType.RECURRING -> Icons.Default.Repeat to CategorySubscriptions
        InsightType.TREND -> Icons.Default.TrendingUp to InsightNeutral
        InsightType.CATEGORY_BREAKDOWN -> Icons.Default.PieChart to CategoryFood
        InsightType.WEEKDAY -> Icons.Default.CalendarMonth to CategoryEntertainment
        InsightType.MERCHANT_SUMMARY -> Icons.Default.Store to CategoryShopping
        InsightType.TIME_OF_DAY -> Icons.Default.Schedule to CategoryTransport
        InsightType.TOP_SPENDING -> Icons.Default.MonetizationOn to Primary
        else -> Icons.Default.Lightbulb to Secondary
    }
}

private fun getInsightTypeLabel(type: String): String {
    return when (InsightType.fromValue(type)) {
        InsightType.RECURRING -> "Recurring"
        InsightType.TREND -> "Trend"
        InsightType.CATEGORY_BREAKDOWN -> "Category"
        InsightType.WEEKDAY -> "Weekday Pattern"
        InsightType.MERCHANT_SUMMARY -> "Merchant"
        InsightType.TIME_OF_DAY -> "Time Pattern"
        InsightType.TOP_SPENDING -> "Top Spending"
        else -> "Insight"
    }
}
