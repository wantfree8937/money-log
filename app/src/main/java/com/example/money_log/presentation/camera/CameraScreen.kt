package com.example.money_log.presentation.camera

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.viewinterop.AndroidView
import com.example.money_log.core.utils.ReceiptAnalyzer
import com.example.money_log.core.utils.CameraManager
import com.example.money_log.ui.theme.MainGreen
import java.io.File

@Composable
fun CameraScreen(
    onImageCaptured: (File) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    
    // 자동 스캔 상태 관리
    var isAutoDetecting by remember { mutableStateOf(false) }
    val analyzer = remember {
        ReceiptAnalyzer(onReceiptDetected = {
            isAutoDetecting = true
            // 캡처 실행 (약간의 피드백을 위해 지연을 줄 수도 있음)
            onImageCaptured // 실제 캡처는 CameraUIOverlay의 onCapture와 연결된 것과 동일하게 수행
        })
    }
    
    val cameraManager = remember { CameraManager(context, lifecycleOwner, previewView) }

    LaunchedEffect(Unit) {
        cameraManager.startCamera(analyzer)
    }

    // analyzer의 콜백 내용을 완성하기 위해 수정
    LaunchedEffect(isAutoDetecting) {
        if (isAutoDetecting) {
            cameraManager.takePhoto(onImageCaptured)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 카메라 프리뷰
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        )

        // 오버레이 및 컨트롤
        CameraUIOverlay(
            onClose = onClose,
            onCapture = { cameraManager.takePhoto(onImageCaptured) },
            isAutoDetecting = isAutoDetecting
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraUIOverlay(onClose: () -> Unit, onCapture: () -> Unit, isAutoDetecting: Boolean = false) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
            }
            Text(
                "영수증 스캔", 
                color = Color.White, 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { /* Flash */ }) {
                Icon(Icons.Default.FlashOn, contentDescription = "플래시", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 가이드 프레임
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(0.7f)
                .align(Alignment.CenterHorizontally)
                .border(
                    2.dp, 
                    if (isAutoDetecting) MainGreen else Color.White.copy(alpha = 0.5f), 
                    RoundedCornerShape(20.dp)
                )
        ) {
            // 모서리
            GuideCorners()
            
            // 스캔 라인 애니메이션
            ScanningLine()
        }
        
        Text(
            if (isAutoDetecting) "영수증 감지됨! 촬영 중..." else "영수증을 가이드 안에 맞춰주세요",
            color = if (isAutoDetecting) MainGreen else Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 24.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        // 하단 컨트롤
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraControlButton(Icons.Default.Image, "갤러리")
            
            // 셔터 버튼
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                onClick = onCapture
            ) {
                Box(
                    modifier = Modifier.padding(8.dp).clip(CircleShape).background(Color.White)
                )
            }

            CameraControlButton(Icons.Default.Cameraswitch, "카메라 전환")
        }
    }
}

@Composable
fun GuideCorners() {
    Box(modifier = Modifier.fillMaxSize()) {
        val cornerSize = 40.dp
        val strokeWidth = 4.dp
        
        // Top Left
        Box(modifier = Modifier.size(cornerSize).align(Alignment.TopStart)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).background(MainGreen))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).background(MainGreen))
        }
        // Top Right
        Box(modifier = Modifier.size(cornerSize).align(Alignment.TopEnd)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).align(Alignment.TopEnd).background(MainGreen))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).background(MainGreen))
        }
        // Bottom Left
        Box(modifier = Modifier.size(cornerSize).align(Alignment.BottomStart)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).background(MainGreen))
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).align(Alignment.BottomStart).background(MainGreen))
        }
        // Bottom Right
        Box(modifier = Modifier.size(cornerSize).align(Alignment.BottomEnd)) {
            Box(modifier = Modifier.fillMaxHeight().width(strokeWidth).align(Alignment.TopEnd).background(MainGreen)) // Fixed alignment here too
            Box(modifier = Modifier.fillMaxWidth().height(strokeWidth).align(Alignment.BottomEnd).background(MainGreen)) // Fixed alignment
        }
    }
}

@Composable
fun ScanningLine() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lineAnimation"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val height = maxHeight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .offset(y = height * offsetY)
                .background(MainGreen.copy(alpha = 0.5f))
                .border(1.dp, MainGreen)
        )
    }
}

@Composable
fun CameraControlButton(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.padding(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}
