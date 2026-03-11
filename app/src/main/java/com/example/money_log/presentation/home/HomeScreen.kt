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
    onDeleteReceipt: (Receipt) -> Unit
) {
    Scaffold(
        bottomBar = {
            MoneyLogBottomNavigation(onAddClick)
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
            // Top Padding for Status Bar if topBar is removed
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total Spending Card
            TotalSpendingCard(monthlyTotal)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Breakdown using real data
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
            
            // Recent Transactions Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "내역", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { /* View All */ }) {
                    Text("전체보기", color = MainGreen)
                }
            }
            
            // Transactions List
            TransactionList(receipts, onDeleteReceipt)
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
            
            // Wallet Icon overlay
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
            
            // Donut Chart showing real proportional data
            Box(
                modifier = Modifier.size(160.dp).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 20.dp,
                    color = Color.LightGray.copy(alpha = 0.2f),
                    trackColor = Color.Transparent
                )
                
                // Overlay segments (simplified representation using multiple indicators)
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
            
            // Row-based Legend for readability
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
fun TransactionList(receipts: List<Receipt>, onDelete: (Receipt) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        receipts.take(5).forEach { receipt ->
            TransactionItem(receipt)
        }
    }
}

@Composable
fun TransactionItem(receipt: Receipt) {
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
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
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
fun MoneyLogBottomNavigation(onCameraClick: () -> Unit) {
    NavigationBar(
        containerColor = SurfaceWhite,
        modifier = Modifier.height(80.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
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
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.History, contentDescription = "내역") },
            label = { Text("내역") }
        )
        
        // Circular Camera Button in the center
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
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "예산") },
            label = { Text("예산") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Settings, contentDescription = "설정") },
            label = { Text("설정") }
        )
    }
}
