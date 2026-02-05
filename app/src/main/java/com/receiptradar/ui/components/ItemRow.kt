package com.receiptradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.receiptradar.data.db.entity.Category
import com.receiptradar.data.db.entity.Item
import com.receiptradar.ui.theme.*

@Composable
fun ItemRow(
    item: Item,
    category: Category?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category color indicator
        Box(
            modifier = Modifier
                .size(4.dp, 40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(getCategoryColor(category?.id))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Item details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity if > 1
                if (item.quantity > 1) {
                    Text(
                        text = "×${item.quantity.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Category chip
                category?.let {
                    CategoryChip(category = it)
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Price
        Text(
            text = formatPrice(item.price),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Edit button
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit item",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val chipModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    
    Surface(
        modifier = chipModifier,
        shape = RoundedCornerShape(16.dp),
        color = getCategoryColor(category.id).copy(alpha = 0.15f)
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = getCategoryColor(category.id),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getCategoryColor(categoryId: Long?): androidx.compose.ui.graphics.Color {
    return when (categoryId) {
        1L -> CategoryFood
        2L -> CategoryGroceries
        3L -> CategoryTransport
        4L -> CategoryShopping
        5L -> CategoryEntertainment
        6L -> CategorySubscriptions
        7L -> CategoryUtilities
        8L -> CategoryHealthcare
        9L -> CategoryEducation
        else -> CategoryOther
    }
}

private fun formatPrice(priceInPaise: Long): String {
    val price = priceInPaise / 100.0
    return "$${String.format("%.2f", price)}"
}
