package com.example.money_log

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.money_log.core.utils.OcrManager
import com.example.money_log.presentation.viewmodel.MainViewModel
import com.example.money_log.ui.camera.CameraManager
import com.example.money_log.ui.components.ReceiptList
import com.example.money_log.ui.theme.MoneyLogTheme
import kotlinx.coroutines.launch
import java.io.File

import com.example.money_log.presentation.ui.HomeScreen
import com.example.money_log.presentation.ui.CameraScreen
import com.example.money_log.presentation.ui.ReceiptDetailsScreen

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
                        val textLines = OcrManager.recognizeText(context, Uri.fromFile(file))
                        viewModel.processOcrResult(textLines, file.absolutePath)
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
