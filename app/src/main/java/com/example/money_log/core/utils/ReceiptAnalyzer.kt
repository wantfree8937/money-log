package com.example.money_log.core.utils

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

/**
 * 실시간으로 프레임을 분석하여 영수증 키워드를 감지하는 분석기
 */
class ReceiptAnalyzer(
    private val onReceiptDetected: () -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    private var isDetected = false
    private var lastAnalysisTime = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        // 성능을 위해 1초에 2번만 분석 (500ms 간격)
        if (currentTime - lastAnalysisTime < 500 || isDetected) {
            imageProxy.close()
            return
        }

        lastAnalysisTime = currentTime
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // 영수증으로 판단할 수 있는 키워드 검색
                    val keywords = listOf("금액", "결제", "합계", "영수증")
                    val fullText = visionText.text
                    
                    val detectedCount = keywords.count { fullText.contains(it) }
                    
                    // 2개 이상의 키워드가 발견되면 영수증으로 간주하고 콜백 실행
                    if (detectedCount >= 1 && !isDetected) {
                        isDetected = true
                        onReceiptDetected()
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
    
    fun reset() {
        isDetected = false
    }
}
