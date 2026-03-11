package com.example.money_log

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.money_log.core.utils.ImageProcessor
import com.example.money_log.core.utils.OcrManager
import com.example.money_log.presentation.viewmodel.MainViewModel
import com.example.money_log.ui.theme.MoneyLogTheme
import kotlinx.coroutines.launch

import com.example.money_log.presentation.home.HomeScreen
import com.example.money_log.presentation.camera.CameraScreen
import com.example.money_log.presentation.receipt_detail.ReceiptDetailsScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.CAMERA)
        }

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
    
    var showCamera by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreen(
            receipts = receipts,
            monthlyTotal = monthlyTotal,
            onAddClick = { showCamera = true },
            onDeleteReceipt = { viewModel.deleteReceipt(it) }
        )

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
                    viewModel.clearParsedReceipt()
                    showCamera = true 
                },
                onBack = { viewModel.clearParsedReceipt() }
            )
        }
    }
}
