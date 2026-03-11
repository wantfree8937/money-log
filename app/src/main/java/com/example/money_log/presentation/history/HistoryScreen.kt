package com.example.money_log.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.money_log.domain.model.Receipt
import com.example.money_log.presentation.home.TransactionItem
import com.example.money_log.ui.theme.BackgroundGray
import com.example.money_log.ui.theme.SurfaceWhite
import com.example.money_log.presentation.home.MoneyLogBottomNavigation

/**
 * 모든 영수증 내역을 보여주는 화면
 */
@OptIn( ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    receipts: List<Receipt>,
    onReceiptClick: (Receipt) -> Unit,
    onDeleteSelected: (List<Receipt>) -> Unit,
    onBack: () -> Unit,
    onCameraClick: () -> Unit,
    onScreenSelected: (String) -> Unit
) {
    var isSelectionMode by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Int>() }

    // 삭제 확인 팝업
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("내역 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("선택한 ${selectedIds.size}개의 영수증 내역을 정말 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        val toDelete = receipts.filter { it.id in selectedIds }
                        onDeleteSelected(toDelete)
                        isSelectionMode = false
                        selectedIds.clear()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("취소")
                }
            },
            containerColor = SurfaceWhite
        )
    }

    // 선택 모드를 종료하고 선택 목록을 초기화하는 함수
    val exitSelectionMode = {
        isSelectionMode = false
        selectedIds.clear()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text("${selectedIds.size}개 선택됨", fontWeight = FontWeight.Bold)
                    } else {
                        Text("내역", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = exitSelectionMode) {
                            Icon(Icons.Default.Close, contentDescription = "취소")
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        if (selectedIds.isNotEmpty()) {
                            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.Red)
                            }
                        }
                    } else {
                        IconButton(onClick = { isSelectionMode = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "선택 삭제")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            // 현재 화면 상태를 반영한 하단 네비게이션 바
            MoneyLogBottomNavigation(
                onCameraClick = onCameraClick,
                currentScreen = "history",
                onScreenSelected = onScreenSelected
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (receipts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("내역이 없습니다.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(receipts) { receipt ->
                        TransactionItem(
                            receipt = receipt,
                            onClick = { onReceiptClick(receipt) },
                            isSelectionMode = isSelectionMode,
                            isSelected = receipt.id in selectedIds,
                            onSelectedChange = { selected ->
                                if (selected) {
                                    selectedIds.add(receipt.id)
                                } else {
                                    selectedIds.remove(receipt.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
