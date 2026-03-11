package com.example.money_log.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.money_log.domain.model.Receipt
import com.example.money_log.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    receipts: List<Receipt>,
    monthlyTotal: Int,
    onAddClick: () -> Unit,
    onReceiptClick: (Receipt) -> Unit,
    onViewAllClick: () -> Unit,
    currentScreen: String = "home",
    onScreenSelected: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            MoneyLogBottomNavigation(
                onCameraClick = onAddClick,
                currentScreen = currentScreen,
                onScreenSelected = onScreenSelected
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // topBar가 제거된 경우 상태 표시줄을 위한 상단 패딩
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))
            
            // 총 지출 카드
            TotalSpendingCard(monthlyTotal)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 실시간 데이터를 사용한 카테고리별 통계
            val categoryStats = remember(receipts) {
                if (receipts.isEmpty() || monthlyTotal == 0) emptyList()
                else receipts.groupBy { it.category }.map { (cat, list) ->
                    val catTotal = list.sumOf { it.amount }
                    val percent = (catTotal.toFloat() / monthlyTotal.toFloat()) * 100f
                    val color = when (cat) {
                        "식비" -> CategoryFood
                        "교통" -> CategoryTransport
                        "생활용품" -> CategoryShopping
                        else -> TextGray
                    }
                    CategoryData(cat, catTotal, percent, color)
                }.sortedByDescending { it.amount }
            }
            
            if (categoryStats.isNotEmpty()) {
                CategoryBreakdownCard(monthlyTotal, categoryStats)
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 최근 내역 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "내역", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onViewAllClick) {
                    Text("전체보기", color = MainGreen)
                }
            }
            
            // 내역 리스트
            TransactionList(receipts, onReceiptClick)
        }
    }
}

@Composable
fun TotalSpendingCard(total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MainGreen)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column {
                Text(
                    "이번 달 총 지불액", 
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "₩ %,d".format(total), 
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingDown, 
                            contentDescription = null, 
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "지난달보다 4.2% 적게 썼어요", 
                            color = Color.White, 
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // 지갑 아이콘 오버레이
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(80.dp).align(Alignment.CenterEnd).offset(x = 10.dp)
            )
        }
    }
}

@Composable
fun CategoryBreakdownCard(total: Int, stats: List<CategoryData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("카테고리별 지출", fontWeight = FontWeight.Bold)
                Text("이번 달", color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 실제 데이터를 보여주는 도넛 차트
            Box(
                modifier = Modifier.size(160.dp).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // 배경 트랙
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 20.dp,
                    color = Color.LightGray.copy(alpha = 0.2f),
                    trackColor = Color.Transparent
                )
                
                // 오버레이 세그먼트 (여러 인디케이터를 사용한 단순화된 표현)
                var currentCombinedPercent = 0f
                stats.take(3).forEach { cat ->
                    val progress = (currentCombinedPercent + cat.percent) / 100f
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 20.dp,
                        color = cat.color,
                        trackColor = Color.Transparent
                    )
                    currentCombinedPercent += cat.percent
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("₩ %,d".format(total), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Text("지출", style = MaterialTheme.typography.labelSmall, color = TextGray)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 가독성을 위한 행 기반 범례
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stats.chunked(3).forEach { rowStats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowStats.forEach { cat ->
                            LegendItem(cat.label, "%.0f%%".format(cat.percent), cat.color)
                        }
                    }
                }
            }
        }
    }
}

data class CategoryData(
    val label: String,
    val amount: Int,
    val percent: Float,
    val color: Color
)

@Composable
fun LegendItem(label: String, percent: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextGray)
        }
        Text(percent, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransactionList(receipts: List<Receipt>, onReceiptClick: (Receipt) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        receipts.take(5).forEach { receipt ->
            TransactionItem(receipt, onClick = { onReceiptClick(receipt) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(receipt: Receipt, onClick: () -> Unit = {}) {
    val icon = when (receipt.category) {
        "식비" -> Icons.Default.Restaurant
        "교통" -> Icons.Default.DirectionsCar
        "생활용품" -> Icons.Default.LocalMall
        else -> Icons.Default.ShoppingBag
    }
    
    val iconBg = when (receipt.category) {
        "식비" -> CategoryFood.copy(alpha = 0.1f)
        "교통" -> CategoryTransport.copy(alpha = 0.1f)
        "생활용품" -> CategoryShopping.copy(alpha = 0.1f)
        else -> TextGray.copy(alpha = 0.1f)
    }
    
    val iconColor = when (receipt.category) {
        "식비" -> CategoryFood
        "교통" -> CategoryTransport
        "생활용품" -> CategoryShopping
        else -> TextGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(receipt.storeName, fontWeight = FontWeight.Bold)
                Text(
                    "오늘, 오후 12:45 • ${receipt.category}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
            
            Text(
                "- ₩ %,d".format(receipt.amount), 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun MoneyLogBottomNavigation(
    onCameraClick: () -> Unit,
    currentScreen: String = "home",
    onScreenSelected: (String) -> Unit = {}
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        modifier = Modifier.height(80.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        NavigationBarItem(
            selected = currentScreen == "home",
            onClick = { onScreenSelected("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "홈") },
            label = { Text("홈") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MainGreen,
                selectedTextColor = MainGreen,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentScreen == "history",
            onClick = { onScreenSelected("history") },
            icon = { Icon(Icons.Default.History, contentDescription = "내역") },
            label = { Text("내역") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MainGreen,
                selectedTextColor = MainGreen,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
        
        // 중앙의 원형 카메라 버튼
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = onCameraClick,
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MainGreen,
                shadowElevation = 8.dp
            ) {
                Icon(
                    Icons.Default.CameraAlt, 
                    contentDescription = "스캔", 
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        NavigationBarItem(
            selected = currentScreen == "statistics",
            onClick = { onScreenSelected("statistics") },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "통계") },
            label = { Text("통계") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MainGreen,
                selectedTextColor = MainGreen,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Settings, contentDescription = "설정") },
            label = { Text("설정") }
        )
    }
}
