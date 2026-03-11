package com.example.money_log.presentation.receipt_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.money_log.domain.model.Receipt
import com.example.money_log.ui.theme.*
import android.graphics.BitmapFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailsScreen(
    receipt: Receipt,
    onSave: (Receipt) -> Unit,
    onRetake: () -> Unit,
    onBack: () -> Unit
) {
    var editedMerchant by remember { mutableStateOf(receipt.storeName) }
    var editedDate by remember { mutableStateOf(receipt.date) }
    var editedAmount by remember { mutableStateOf(receipt.amount.toString()) }
    var editedCategory by remember { mutableStateOf(receipt.category) }

    // 날짜 및 카테고리 선택기 상태
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val categories = listOf("식비", "교통", "쇼핑", "의료", "생활", "주거", "통신", "교육", "기타")
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        editedDate = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCategoryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryPicker = false },
            sheetState = bottomSheetState,
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "카테고리 선택",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        ListItem(
                            headlineContent = { Text(category) },
                            leadingContent = {
                                val icon = when (category) {
                                    "식비" -> Icons.Default.Restaurant
                                    "교통" -> Icons.Default.DirectionsCar
                                    "쇼핑" -> Icons.Default.ShoppingBag
                                    "의료" -> Icons.Default.MedicalServices
                                    "생활" -> Icons.Default.CleaningServices
                                    "주거" -> Icons.Default.Home
                                    "통신" -> Icons.Default.Smartphone
                                    "교육" -> Icons.Default.School
                                    else -> Icons.Default.Category
                                }
                                Icon(icon, contentDescription = null, tint = MainGreen)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editedCategory = category
                                    showCategoryPicker = false
                                }
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상세 정보", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 영수증 이미지 미리보기
            val bitmap = remember(receipt.imagePath) {
                BitmapFactory.decodeFile(receipt.imagePath)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "영수증 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("이미지를 찾을 수 없습니다")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 입력 필드들
            ReceiptInputField(
                label = "가맹점 이름",
                value = editedMerchant,
                onValueChange = { editedMerchant = it },
                icon = Icons.Default.Store
            )
            
            ReceiptInputField(
                label = "날짜",
                value = editedDate,
                onValueChange = { editedDate = it },
                icon = Icons.Default.CalendarToday,
                onIconClick = { showDatePicker = true }
            )
            
            ReceiptInputField(
                label = "합계 금액",
                value = editedAmount,
                onValueChange = { editedAmount = it },
                prefix = "₩ "
            )
            
            ReceiptInputField(
                label = "카테고리",
                value = editedCategory,
                onValueChange = { editedCategory = it },
                icon = Icons.Default.Category,
                isDropdown = true,
                onFieldClick = { showCategoryPicker = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 작업 버튼들
            Button(
                onClick = { 
                    onSave(receipt.copy(
                        storeName = editedMerchant,
                        date = editedDate,
                        amount = editedAmount.toIntOrNull() ?: 0,
                        category = editedCategory,
                        createdAt = if (receipt.createdAt == 0L) System.currentTimeMillis() else receipt.createdAt
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainGreen)
            ) {
                Text("내역 저장", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onRetake,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen, contentColor = PrimaryGreen)
            ) {
                Text("다시 촬영", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    onIconClick: (() -> Unit)? = null,
    onFieldClick: (() -> Unit)? = null,
    prefix: String? = null,
    isDropdown: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            label, 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .let { 
                    if (onFieldClick != null) it.clickable(onClick = onFieldClick) else it 
                },
            enabled = onFieldClick == null,
            readOnly = onFieldClick != null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceWhite,
                focusedContainerColor = SurfaceWhite,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MainGreen,
                disabledBorderColor = Color.Transparent,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                disabledPrefixColor = MaterialTheme.colorScheme.onSurface,
                disabledSuffixColor = MaterialTheme.colorScheme.onSurface
            ),
            trailingIcon = {
                if (isDropdown) {
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = MainGreen)
                } else if (icon != null) {
                    if (onIconClick != null) {
                        IconButton(onClick = onIconClick) {
                            Icon(icon, contentDescription = null, tint = MainGreen)
                        }
                    } else {
                        Icon(icon, contentDescription = null, tint = MainGreen)
                    }
                }
            },
            prefix = prefix?.let { { Text(it, fontWeight = FontWeight.Bold) } },
            singleLine = true
        )
    }
}
