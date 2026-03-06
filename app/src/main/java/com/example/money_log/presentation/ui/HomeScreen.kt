package com.example.money_log.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "MoneyLog", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { /* Open Drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* Profile */ }) {
                        Icon(
                            Icons.Default.AccountCircle, 
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        bottomBar = {
            MoneyLogBottomNavigation()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MainGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total Spending Card
            TotalSpendingCard(monthlyTotal)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Breakdown (Placeholder for now)
            CategoryBreakdownCard()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recent Transactions Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Transactions", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { /* View All */ }) {
                    Text("View All", color = MainGreen)
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
                    "Total Spending (This Month)", 
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$ %,d.00".format(total), // Simplified decimal
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
                            "4.2% less than last month", 
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
fun CategoryBreakdownCard() {
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
                Text("Category Breakdown", fontWeight = FontWeight.Bold)
                Text("Monthly", color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Donut Chart Placeholder (Stylized using Box and CircularProgress)
            Box(
                modifier = Modifier.size(160.dp).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = 0.45f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 20.dp,
                    color = CategoryFood,
                    trackColor = Color.LightGray.copy(alpha = 0.2f)
                )
                CircularProgressIndicator(
                    progress = 0.25f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 20.dp,
                    color = CategoryTransport,
                    trackColor = Color.Transparent
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$1.2k", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Text("SPENT", style = MaterialTheme.typography.labelSmall, color = TextGray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("Food", "45%", CategoryFood)
                LegendItem("Transport", "25%", CategoryTransport)
                LegendItem("Shopping", "30%", CategoryShopping)
            }
        }
    }
}

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
                    "Today, 12:45 PM • ${receipt.category}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
            
            Text(
                "- $ %,d.00".format(receipt.amount), 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun MoneyLogBottomNavigation() {
    NavigationBar(
        containerColor = SurfaceWhite,
        modifier = Modifier.height(80.dp)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
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
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") }
        )
        Spacer(Modifier.weight(1f)) // Center space for FAB
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Budget") },
            label = { Text("Budget") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
