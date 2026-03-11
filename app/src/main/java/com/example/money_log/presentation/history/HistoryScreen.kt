package com.example.money_log.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    receipts: List<Receipt>,
    onBack: () -> Unit,
    onCameraClick: () -> Unit,
    onScreenSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내역", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
                        TransactionItem(receipt = receipt)
                    }
                }
            }
        }
    }
}
