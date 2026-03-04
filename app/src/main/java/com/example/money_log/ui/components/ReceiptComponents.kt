package com.example.money_log.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.money_log.domain.model.Receipt

/**
 * 개별 영수증 아이템 컴포저블
 */
@Composable
fun ReceiptItem(
    receipt: Receipt,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receipt.storeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${receipt.date} | ${receipt.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                AssistChip(
                    onClick = { },
                    label = { Text(receipt.category) },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%,d원".format(receipt.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 영수증 목록 컴포저블
 */
@Composable
fun ReceiptList(
    receipts: List<Receipt>,
    onDelete: (Receipt) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(receipts, key = { it.id }) { receipt ->
            ReceiptItem(
                receipt = receipt,
                onDelete = { onDelete(receipt) }
            )
        }
    }
}
