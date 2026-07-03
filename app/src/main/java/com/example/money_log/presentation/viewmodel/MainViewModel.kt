package com.example.money_log.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.money_log.core.utils.ReceiptParser
import com.example.money_log.data.local.AppDatabase
import com.example.money_log.data.repository.ReceiptRepositoryImpl
import com.example.money_log.domain.model.Receipt
import com.example.money_log.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 앱의 주요 상태와 비즈니스 로직을 연결하는 ViewModel
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReceiptRepositoryImpl(
        AppDatabase.getDatabase(application).receiptDao()
    )

    private val settingsDataStore = com.example.money_log.core.settings.SettingsDataStore(application)

    private val getReceiptsUseCase = GetReceiptsUseCase(repository)
    private val saveReceiptUseCase = SaveReceiptUseCase(repository)
    private val deleteReceiptUseCase = DeleteReceiptUseCase(repository)
    private val getMonthlyTotalUseCase = GetMonthlyTotalUseCase(repository)

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    val receipts: StateFlow<List<Receipt>> = _receipts.asStateFlow()

    private val _monthlyTotal = MutableStateFlow(0)
    val monthlyTotal: StateFlow<Int> = _monthlyTotal.asStateFlow()

    private val _parsedReceipt = MutableStateFlow<Receipt?>(null)
    val parsedReceipt: StateFlow<Receipt?> = _parsedReceipt.asStateFlow()

    val darkMode = settingsDataStore.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val language = settingsDataStore.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ko")
    val categories = settingsDataStore.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("식비", "교통", "쇼핑", "의료", "생활", "주거", "통신", "교육", "기타"))

    // 현재 수정 중인 영수증의 ID (새 영수증이면 0 또는 null)
    private var currentEditingId: Int? = null

    init {
        loadReceipts()
        observeMonthlyTotal()
    }

    // 설정 업데이트 함수들

    fun updateDarkMode(mode: String) = viewModelScope.launch { settingsDataStore.updateDarkMode(mode) }
    fun updateLanguage(lang: String) = viewModelScope.launch { settingsDataStore.updateLanguage(lang) }

    fun addCategory(category: String) {
        val current = categories.value.toMutableList()
        if (!current.contains(category)) {
            current.add(category)
            viewModelScope.launch { settingsDataStore.updateCategories(current) }
        }
    }

    fun deleteCategory(category: String) {
        val current = categories.value.toMutableList()
        if (current.contains(category)) {
            current.remove(category)
            viewModelScope.launch { settingsDataStore.updateCategories(current) }
        }
    }

    private fun loadReceipts() {
        viewModelScope.launch {
            getReceiptsUseCase().collect {
                _receipts.value = it
            }
        }
    }

    fun exportReceiptsToCsv(context: android.content.Context, onComplete: () -> Unit) {
        com.example.money_log.core.utils.ExportUtils.exportReceiptsToCsv(context, _receipts.value, onComplete)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMonthlyTotal() {
        viewModelScope.launch {
            val (startDate, endDate) = calculateDateRange()
            getMonthlyTotalUseCase(startDate, endDate).collect {
                _monthlyTotal.value = it
            }
        }
    }

    /**
     * 이번 달의 첫날과 마지막 날 계산 (1일 ~ 말일)
     */
    private fun calculateDateRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val startCal = Calendar.getInstance()
        startCal.set(Calendar.DAY_OF_MONTH, 1)

        val endCal = Calendar.getInstance()
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))

        return sdf.format(startCal.time) to sdf.format(endCal.time)
    }

    fun processOcrResult(textLines: List<String>, imagePath: String) {
        val parsed = ReceiptParser.parse(textLines, imagePath)
        
        // 날짜와 최종 금액은 살리되 가맹점 이름만 빈 문자열("")로 초기화하여 수동 입력 유도
        val baseParsed = parsed.copy(storeName = "")

        // 카테고리가 현재 설정상의 카테고리 목록에 없으면 '기타' 또는 첫 번째 카테고리로 보정
        val finalCategory = if (categories.value.contains(baseParsed.category)) {
            baseParsed.category
        } else {
            if (categories.value.contains("기타")) "기타" else (categories.value.firstOrNull() ?: "기타")
        }
        val clearedParsed = baseParsed.copy(category = finalCategory)
        
        // 만약 기존 영수증을 수정(재촬영) 중이었다면 해당 ID를 유지
        _parsedReceipt.value = currentEditingId?.let { id ->
            clearedParsed.copy(id = id)
        } ?: clearedParsed
    }

    fun setSelectedReceipt(receipt: Receipt) {
        _parsedReceipt.value = receipt
        currentEditingId = receipt.id
    }

    fun saveReceipt(receipt: Receipt) {
        viewModelScope.launch {
            saveReceiptUseCase(receipt)
            _parsedReceipt.value = null
            currentEditingId = null
        }
    }

    fun clearParsedReceipt() {
        _parsedReceipt.value = null
        currentEditingId = null
    }

    fun prepareRetake() {
        _parsedReceipt.value = null
        // currentEditingId는 유지하여 재촬영 후에도 기존 항목 수정이 가능하게 함
    }

    /**
     * 직접 입력을 위해 빈 영수증 데이터 생성
     */
    fun startManualEntry() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        _parsedReceipt.value = Receipt(
            storeName = "",
            amount = 0,
            date = today,
            category = "기타",
            paymentMethod = "카드",
            imagePath = "", // 직접 입력 시 이미지는 없음
            createdAt = System.currentTimeMillis()
        )
        currentEditingId = null
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            deleteReceiptUseCase(receipt)
            _parsedReceipt.value = null
            currentEditingId = null
        }
    }

    fun deleteSelectedReceipts(selectedReceipts: List<Receipt>) {
        viewModelScope.launch {
            selectedReceipts.forEach {
                deleteReceiptUseCase(it)
            }
        }
    }
}
