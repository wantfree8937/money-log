package com.example.money_log.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.money_log.domain.model.Receipt
import com.example.money_log.presentation.home.TransactionItem
import com.example.money_log.ui.theme.MainGreen
import com.example.money_log.ui.theme.BackgroundGray
import com.example.money_log.ui.theme.SurfaceWhite
import com.example.money_log.presentation.home.MoneyLogBottomNavigation
import java.text.SimpleDateFormat
import java.util.*

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
    onManualEntryClick: () -> Unit,
    onScreenSelected: (String) -> Unit
) {
    var isSelectionMode by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Int>() }
    
    // 추가 옵션 선택 바텀 시트
    var showAddOptions by remember { mutableStateOf(false) }
    val addOptionsSheetState = rememberModalBottomSheetState()
    
    // 년월 직접 선택기 상태
    var showMonthPicker by remember { mutableStateOf(false) }
    val monthPickerSheetState = rememberModalBottomSheetState()
    
    // 현재 선택된 년월 (기본값: 이번 달)
    var selectedMonth by remember { 
        mutableStateOf(SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())) 
    }

    // 선택된 월에 해당하는 내역만 필터링
    val filteredReceipts = receipts.filter { it.date.startsWith(selectedMonth) }

    // 추가 옵션 선택 바텀 시트 UI
    if (showAddOptions) {
        ModalBottomSheet(
            onDismissRequest = { showAddOptions = false },
            sheetState = addOptionsSheetState,
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "내역 추가 방법 선택",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                
                ListItem(
                    headlineContent = { Text("영수증 촬영") },
                    supportingContent = { Text("영수증을 촬영하고 내역을 입력합니다") },
                    leadingContent = { 
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MainGreen) 
                    },
                    modifier = Modifier.clickable {
                        showAddOptions = false
                        onCameraClick()
                    }
                )
                
                ListItem(
                    headlineContent = { Text("직접 입력") },
                    supportingContent = { Text("가맹점, 금액 등을 직접 입력합니다") },
                    leadingContent = { 
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = MainGreen) 
                    },
                    modifier = Modifier.clickable {
                        showAddOptions = false
                        onManualEntryClick()
                    }
                )
            }
        }
    }

    // 년월 직접 선택 바텀 시트
    if (showMonthPicker) {
        ModalBottomSheet(
            onDismissRequest = { showMonthPicker = false },
            sheetState = monthPickerSheetState,
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    "년월 선택",
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 년도 선택 (최근 5년 ~ 내년)
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val years = (currentYear - 4..currentYear + 1).toList()
                    
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(years) { year ->
                            val isSelected = selectedMonth.startsWith(year.toString())
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val monthPart = selectedMonth.split("-")[1]
                                        selectedMonth = "$year-$monthPart"
                                    },
                                color = if (isSelected) BackgroundGray else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "${year}년",
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MainGreen else Color.Black
                                )
                            }
                        }
                    }
                    
                    // 월 선택
                    val months = (1..12).toList()
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(months) { month ->
                            val monthStr = "%02d".format(month)
                            val isSelected = selectedMonth.endsWith(monthStr)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val yearPart = selectedMonth.split("-")[0]
                                        selectedMonth = "$yearPart-$monthStr"
                                        showMonthPicker = false
                                    },
                                color = if (isSelected) BackgroundGray else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "${month}월",
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MainGreen else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }

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
                onAddButtonClick = { showAddOptions = true },
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
            // 년월 선택기
            if (!isSelectionMode) {
                Surface(
                    color = SurfaceWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            val current = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth) ?: Date()
                            cal.time = current
                            cal.add(Calendar.MONTH, -1)
                            selectedMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달")
                        }
                        
                        Text(
                            text = selectedMonth.replace("-", "년 ") + "월",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showMonthPicker = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            val current = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth) ?: Date()
                            cal.time = current
                            cal.add(Calendar.MONTH, 1)
                            selectedMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "다음 달")
                        }
                    }
                }
            }

            if (filteredReceipts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${selectedMonth.replace("-", "년 ")}월 내역이 없습니다.", color = Color.Gray)
                }
            } else {
                // 내역을 년월별로 그룹화
                val groupedReceipts = filteredReceipts.groupBy { 
                    it.date.take(7).replace("-", "년 ") + "월"
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedReceipts.forEach { (_, monthReceipts) ->
                        // 해당 월의 내역들
                        items(monthReceipts) { receipt ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
    }
}
