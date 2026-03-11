package com.example.money_log.presentation.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.money_log.domain.model.Receipt
import com.example.money_log.presentation.home.MoneyLogBottomNavigation
import com.example.money_log.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    receipts: List<Receipt>,
    onAddClick: () -> Unit,
    currentScreen: String = "statistics",
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
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))

            // 상단 타이틀 및 월 선택 (임시)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "지출 통계",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    onClick = { /* 월 선택 로직 */ }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2026년 3월", style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 요약 카드
            StatsSummaryCard(receipts)

            Spacer(modifier = Modifier.height(24.dp))

            // 월별 추이 그래프
            MonthlyTrendCard()

            Spacer(modifier = Modifier.height(24.dp))

            // 카테고리별 상세 분석
            CategoryAnalysisCard(receipts)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatsSummaryCard(receipts: List<Receipt>) {
    val totalAmount = receipts.sumOf { it.amount }
    val formatter = NumberFormat.getCurrencyInstance(Locale.KOREA)
    
    // 가장 많이 지출한 카테고리 계산
    val topCategory = receipts.groupBy { it.category }
        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
        ?.key ?: "없음"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MainGreen)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("이번 달 총 지출", color = Color.White.copy(alpha = 0.8f))
            Text(
                formatter.format(totalAmount),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp).size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "주요 소비: $topCategory",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MonthlyTrendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "월별 지출 추이",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // 단순한 바 차트 구현
            Row(
                modifier = Modifier.height(150.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val data = listOf(0.4f, 0.6f, 0.5f, 0.8f, 0.3f, 0.9f)
                val months = listOf("10월", "11월", "12월", "1월", "2월", "3월")
                
                data.forEachIndexed { index, value ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(value)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (index == 5) MainGreen else MainGreen.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(months[index], fontSize = 10.sp, color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryAnalysisCard(receipts: List<Receipt>) {
    val categoryTotals = receipts.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val totalAmount = receipts.sumOf { it.amount }.coerceAtLeast(1)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "카테고리별 분석",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // 도넛 차트 및 리스트
            categoryTotals.forEach { (category, amount) ->
                val percentage = (amount.toFloat() / totalAmount)
                CategoryProgressItem(category, amount, percentage)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CategoryProgressItem(category: String, amount: Int, percentage: Float) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.KOREA)
    val icon = when (category) {
        "식비" -> Icons.Default.Restaurant
        "교통" -> Icons.Default.DirectionsCar
        "쇼핑" -> Icons.Default.ShoppingBag
        "기타" -> Icons.Default.Category
        else -> Icons.Default.Payments
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MainGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(category, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            }
            Text(
                formatter.format(amount),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = MainGreen,
            trackColor = MainGreen.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${(percentage * 100).toInt()}%",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            fontSize = 11.sp,
            color = TextGray
        )
    }
}
