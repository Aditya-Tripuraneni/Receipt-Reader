package com.receiptradar.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.receiptradar.data.db.entity.Category
import com.receiptradar.data.db.entity.Item
import com.receiptradar.ui.components.CategoryChip
import com.receiptradar.ui.components.ConfidenceIndicator
import com.receiptradar.ui.components.ItemRow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReceiptDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Navigate back when deleted
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.receipt?.merchant ?: "Receipt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.receipt != null -> {
                    ReceiptDetailContent(
                        receipt = uiState.receipt!!,
                        items = uiState.items,
                        categories = uiState.categories,
                        onEditItem = { viewModel.showEditDialog(it) }
                    )
                }
            }
            
            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Receipt?") },
                text = { Text("This action cannot be undone. All items in this receipt will be deleted.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteReceipt()
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Edit item dialog
        uiState.editingItem?.let { item ->
            EditItemDialog(
                item = item,
                categories = uiState.categories,
                onDismiss = { viewModel.hideEditDialog() },
                onSave = { name, price, quantity, categoryId ->
                    viewModel.updateItem(item.id, name, price, quantity, categoryId)
                    viewModel.hideEditDialog()
                },
                onDelete = {
                    viewModel.deleteItem(item.id)
                    viewModel.hideEditDialog()
                }
            )
        }
    }
}

@Composable
private fun ReceiptDetailContent(
    receipt: com.receiptradar.data.db.entity.Receipt,
    items: List<Item>,
    categories: List<Category>,
    onEditItem: (Item) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Receipt image
        receipt.imagePath?.let { imagePath ->
            item {
                AsyncImage(
                    model = imagePath,
                    contentDescription = "Receipt image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Receipt summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = receipt.merchant,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDate(receipt.date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = formatAmount(receipt.total, receipt.currency),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    receipt.confidence?.let { confidence ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ConfidenceIndicator(confidence = confidence)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Items header
        item {
            Text(
                text = "Items (${items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Items list
        items(
            items = items,
            key = { it.id }
        ) { item ->
            val category = categories.find { it.id == item.categoryId }
            ItemRow(
                item = item,
                category = category,
                onEditClick = { onEditItem(item) }
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }
        
        // Items total
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Items Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatAmount(
                        items.sumOf { (it.price * it.quantity).toLong() },
                        receipt.currency
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EditItemDialog(
    item: Item,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Long?) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(item.categoryId) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Category") },
        text = {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CategoryChip(
                            category = category,
                            onClick = { selectedCategoryId = category.id }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedCategoryId) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(epochMillis: Long): String {
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    return dateFormat.format(Date(epochMillis))
}

private fun formatAmount(amountInPaise: Long, currency: String): String {
    val amount = amountInPaise / 100.0
    val symbol = when (currency) {
        "INR" -> "₹"
        "USD" -> "$"
        "CAD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "AUD" -> "$"
        else -> "$"
    }
    return "$symbol${String.format("%.2f", amount)}"
}
