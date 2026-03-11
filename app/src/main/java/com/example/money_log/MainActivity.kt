package com.example.money_log

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.money_log.core.utils.ImageProcessor
import com.example.money_log.core.utils.OcrManager
import com.example.money_log.presentation.viewmodel.MainViewModel
import com.example.money_log.ui.theme.MoneyLogTheme
import kotlinx.coroutines.launch

import com.example.money_log.presentation.home.HomeScreen
import com.example.money_log.presentation.camera.CameraScreen
import com.example.money_log.presentation.history.HistoryScreen
import com.example.money_log.presentation.statistics.StatisticsScreen
import com.example.money_log.presentation.receipt_detail.ReceiptDetailsScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 권한이 부여되지 않은 경우 카메라 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.CAMERA)
        }

        // 네비게이션 바를 숨기고 스와이프 시에만 표시 (몰입 모드)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        setContent {
            MoneyLogTheme {
                MainAppHost(viewModel)
            }
        }
    }
}

@Composable
fun MainAppHost(viewModel: MainViewModel) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val parsedReceipt by viewModel.parsedReceipt.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var currentScreen by remember { mutableStateOf("home") }
    var showCamera by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // 시스템 뒤로가기 버튼 처리
    BackHandler {
        when {
            parsedReceipt != null -> viewModel.clearParsedReceipt()
            showCamera -> showCamera = false
            currentScreen == "history" || currentScreen == "statistics" -> currentScreen = "home"
            currentScreen == "home" -> showExitDialog = true
        }
    }

    if (showExitDialog) {
        ExitConfirmDialog(
            onConfirm = { 
                (context as? android.app.Activity)?.finish() 
            },
            onDismiss = { showExitDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "home" -> {
                HomeScreen(
                    receipts = receipts,
                    monthlyTotal = monthlyTotal,
                    onAddClick = { showCamera = true },
                    onReceiptClick = { viewModel.setSelectedReceipt(it) },
                    onViewAllClick = { currentScreen = "history" },
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
            "history" -> {
                HistoryScreen(
                    receipts = receipts,
                    onReceiptClick = { viewModel.setSelectedReceipt(it) },
                    onDeleteSelected = { viewModel.deleteSelectedReceipts(it) },
                    onBack = { currentScreen = "home" },
                    onCameraClick = { showCamera = true },
                    onScreenSelected = { currentScreen = it }
                )
            }
            "statistics" -> {
                StatisticsScreen(
                    receipts = receipts,
                    onAddClick = { showCamera = true },
                    onScreenSelected = { currentScreen = it }
                )
            }
        }

        if (showCamera) {
            CameraScreen(
                onImageCaptured = { file ->
                    showCamera = false
                    scope.launch {
                        // 이미지 전처리 (크롭, 그레이스케일, 대비 개선)
                        val processedFile = ImageProcessor.processImage(context, file)
                        
                        // 전처리된 이미지로 OCR 실행
                        val textLines = OcrManager.recognizeText(context, Uri.fromFile(processedFile))
                        
                        // 결과 처리 (디테일 화면에는 전처리된 이미지를 보여줌)
                        viewModel.processOcrResult(textLines, processedFile.absolutePath)
                    }
                },
                onClose = { showCamera = false }
            )
        }

        parsedReceipt?.let { receipt ->
            ReceiptDetailsScreen(
                receipt = receipt,
                onSave = { viewModel.saveReceipt(it) },
                onRetake = { 
                    viewModel.prepareRetake()
                    showCamera = true 
                },
                onBack = { viewModel.clearParsedReceipt() }
            )
        }
    }
}

/**
 * 앱 종료 확인 팝업
 */
@Composable
fun ExitConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("앱 종료", fontWeight = FontWeight.Bold) },
        text = { Text("머니로그를 종료하시겠습니까?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = com.example.money_log.ui.theme.MainGreen)
            ) {
                Text("종료", color = androidx.compose.ui.graphics.Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = com.example.money_log.ui.theme.TextGray)
            }
        },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        containerColor = com.example.money_log.ui.theme.SurfaceWhite
    )
}
