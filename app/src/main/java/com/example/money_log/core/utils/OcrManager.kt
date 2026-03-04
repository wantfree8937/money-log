package com.example.money_log.core.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * ML Kit을 사용하여 이미지에서 텍스트를 추출하는 매니저
 */
object OcrManager {
    private val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    suspend fun recognizeText(context: Context, imageUri: Uri): List<String> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            result.textBlocks.flatMap { block ->
                block.lines.map { it.text }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
