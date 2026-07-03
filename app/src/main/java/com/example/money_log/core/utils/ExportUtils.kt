package com.example.money_log.core.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.money_log.domain.model.Receipt
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 데이터를 CSV로 내보내기 위한 유틸리티
 */
object ExportUtils {

    /**
     * 영수증 목록을 CSV 파일로 컴파일하여 기기 공용 Download 폴더에 영구 저장합니다.
     */
    fun exportReceiptsToCsv(context: Context, receipts: List<Receipt>, onComplete: () -> Unit) {
        val fileName = "MoneyLog_Export_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri).use { outputStream ->
                        outputStream?.write("\uFEFF".toByteArray()) // CSV 한글 깨짐 방지용 BOM 주입
                        outputStream?.write("날짜,가맹점,금액,카테고리,결제수단\n".toByteArray(Charsets.UTF_8))
                        receipts.forEach { receipt ->
                            val line = "${receipt.date},${receipt.storeName},${receipt.amount},${receipt.category},${receipt.paymentMethod}\n"
                            outputStream?.write(line.toByteArray(Charsets.UTF_8))
                        }
                    }
                    android.widget.Toast.makeText(context, "다운로드 폴더에 CSV 파일이 저장되었습니다.", android.widget.Toast.LENGTH_LONG).show()
                    onComplete()
                } else {
                    android.widget.Toast.makeText(context, "파일 생성에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                // Android 9 이하 레거시 기기 대응
                val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }
                val targetFile = File(downloadDir, fileName)
                targetFile.writer(Charsets.UTF_8).use { writer ->
                    writer.write("\uFEFF") // BOM
                    writer.write("날짜,가맹점,금액,카테고리,결제수단\n")
                    receipts.forEach { receipt ->
                        val line = "${receipt.date},${receipt.storeName},${receipt.amount},${receipt.category},${receipt.paymentMethod}\n"
                        writer.write(line)
                    }
                }
                android.widget.Toast.makeText(context, "다운로드 폴더에 CSV 파일이 저장되었습니다.", android.widget.Toast.LENGTH_LONG).show()
                onComplete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "저장 중 오류가 발생했습니다: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}
