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

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            MoneyLogTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var showCamera by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Money Log") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    showCamera = true 
                } else {
                    Toast.makeText(context, "카메라 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 이번 달 지출 요약 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "이번 달 지출 현황", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "%,d원".format(monthlyTotal),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "최근 지출 내역",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            ReceiptList(
                receipts = receipts,
                onDelete = { viewModel.deleteReceipt(it) }
            )
        }

        if (showCamera) {
            CameraOverlay(
                onImageCaptured = { file ->
                    showCamera = false
                    scope.launch {
                        val textLines = OcrManager.recognizeText(context, Uri.fromFile(file))
                        viewModel.processOcrResult(textLines, file.absolutePath)
                        Toast.makeText(context, "영수증이 분석되어 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                },
                onClose = { showCamera = false }
            )
        }
    }
}

@Composable
fun CameraOverlay(
    onImageCaptured: (File) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraManager = remember { CameraManager(context, lifecycleOwner, previewView) }

    LaunchedEffect(Unit) {
        cameraManager.startCamera()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Button(
                onClick = { cameraManager.takePhoto(onImageCaptured) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("영수증 촬영")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("취소", color = Color.White)
            }
        }
    }
}