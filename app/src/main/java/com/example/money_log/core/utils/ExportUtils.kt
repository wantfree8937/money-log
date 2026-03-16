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
     * 영수증 목록을 CSV 파일로 저장하고 공유 인텐트를 반환합니다.
     */
    fun exportReceiptsToCsv(context: Context, receipts: List<Receipt>) {
        val fileName = "MoneyLog_Export_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            file.writer().use { writer ->
                // 헤더 작성 (BOM 추가하여 엑셀 한글 깨짐 방지)
                writer.write("\uFEFF")
                writer.write("날짜,가맹점,금액,카테고리,결제수단\n")
                
                receipts.forEach { receipt ->
                    val line = "${receipt.date},${receipt.storeName},${receipt.amount},${receipt.category},${receipt.paymentMethod}\n"
                    writer.write(line)
                }
            }
            
            shareFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(context: Context, file: File) {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "MoneyLog 지출 내역")
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "파일 보내기"))
    }
}
