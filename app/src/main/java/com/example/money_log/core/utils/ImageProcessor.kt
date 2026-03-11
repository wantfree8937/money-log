package com.example.money_log.core.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * OCR 인식률을 높이기 위해 이미지를 전처리(크롭, 그레이스케일, 대비 수정)하는 유틸리티
 */
object ImageProcessor {

    /**
     * 이미지를 전처리하고 처리된 파일 경로를 반환합니다.
     */
    fun processImage(context: Context, inputFile: File): File {
        return try {
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return inputFile
            
            // 1. 가이드 영역에 맞게 크롭 (중앙 85% 가로, 0.7 종횡비)
            val croppedBitmap = cropToGuide(bitmap)
            
            // 2. 그레이스케일 및 대비 증가
            val processedBitmap = applyFilters(croppedBitmap)
            
            // 3. 파일로 저장
            val outputFile = File(context.cacheDir, "processed_${inputFile.name}")
            FileOutputStream(outputFile).use { out ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            croppedBitmap.recycle()
            processedBitmap.recycle()
            
            outputFile
        } catch (e: Exception) {
            Log.e("ImageProcessor", "이미지 처리 실패", e)
            inputFile
        }
    }

    private fun cropToGuide(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // 0.85% width, 0.7 aspect ratio (CameraScreen의 가이드와 일치)
        val targetWidth = (width * 0.85f).toInt()
        val targetHeight = (targetWidth / 0.7f).toInt()
        
        // 크기가 원본보다 크지 않도록 조정
        val finalWidth = if (targetWidth > width) width else targetWidth
        val finalHeight = if (targetHeight > height) height else targetHeight
        
        val startX = (width - finalWidth) / 2
        val startY = (height - finalHeight) / 2
        
        return Bitmap.createBitmap(bitmap, startX, startY, finalWidth, finalHeight)
    }

    private fun applyFilters(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val processed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(processed)
        val paint = Paint()
        
        // 1. 그레이스케일 매트릭스
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        
        // 2. 대비(Contrast) 증가 매트릭스
        val contrast = 1.5f
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastMatrix = floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        cm.postConcat(ColorMatrix(contrastMatrix))
        
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return processed
    }
}
