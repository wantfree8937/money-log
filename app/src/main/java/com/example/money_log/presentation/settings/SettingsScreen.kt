package com.example.money_log.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.money_log.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    startDay: Int,
    autoSave: Boolean,
    darkMode: String,
    language: String,
    onStartDayChange: (Int) -> Unit,
    onAutoSaveChange: (Boolean) -> Unit,
    onDarkModeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onCategoryEditClick: () -> Unit,
    onExportClick: () -> Unit,
    onBack: () -> Unit
) {
    var showStartDayDialog by remember { mutableStateOf(false) }
    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showStartDayDialog) {
        AlertDialog(
            onDismissRequest = { showStartDayDialog = false },
            title = { Text("월 시작일 선택", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    (1..28).forEach { day ->
                        Text(
                            text = "${day}일",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onStartDayChange(day)
                                    showStartDayDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStartDayDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (showDarkModeDialog) {
        val options = listOf("system" to "시스템 설정", "light" to "라이트 모드", "dark" to "다크 모드")
        AlertDialog(
            onDismissRequest = { showDarkModeDialog = false },
            title = { Text("다크모드 설정", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    options.forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDarkModeChange(mode)
                                    showDarkModeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            RadioButton(selected = darkMode == mode, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDarkModeDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (showLanguageDialog) {
        val options = listOf("ko" to "한국어", "en" to "English")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("언어 설정", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    options.forEach { (lang, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            RadioButton(selected = language == lang, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // 가계부 설정 섹션
            SettingsSection(title = "가계부 설정") {
                SettingsItem(
                    icon = Icons.Default.CalendarToday,
                    title = "월 시작일 설정",
                    value = "${startDay}일",
                    onClick = { showStartDayDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.Category,
                    title = "카테고리 편집",
                    onClick = onCategoryEditClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 데이터 관리 섹션
            SettingsSection(title = "데이터 관리") {
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "엑셀(CSV) 내보내기",
                    onClick = onExportClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 인식 설정 섹션
            SettingsSection(title = "인식 설정") {
                SettingsSwitchItem(
                    icon = Icons.Default.AutoFixHigh,
                    title = "인식 결과 자동 저장",
                    description = "OCR 인식 후 바로 저장합니다.",
                    checked = autoSave,
                    onCheckedChange = onAutoSaveChange
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 디스플레이 및 기타 섹션
            SettingsSection(title = "디스플레이 및 기타") {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "다크모드",
                    value = when(darkMode) {
                        "light" -> "라이트 모드"
                        "dark" -> "다크 모드"
                        else -> "시스템 설정"
                    },
                    onClick = { showDarkModeDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "언어 설정",
                    value = if (language == "ko") "한국어" else "English",
                    onClick = { showLanguageDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            fontSize = 14.sp,
            color = TextGray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MainGreen.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MainGreen,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        if (value != null) {
            Text(
                value,
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MainGreen.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MainGreen,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MainGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
