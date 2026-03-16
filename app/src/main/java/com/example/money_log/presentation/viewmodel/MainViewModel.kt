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

    // 설정 상태 노출
    val startDay = settingsDataStore.startDay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val autoSave = settingsDataStore.autoSave.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val darkMode = settingsDataStore.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val language = settingsDataStore.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ko")

    // 현재 수정 중인 영수증의 ID (새 영수증이면 0 또는 null)
    private var currentEditingId: Int? = null

    init {
        loadReceipts()
        loadMonthlyTotal()
    }

    // 설정 업데이트 함수들
    fun updateStartDay(day: Int) = viewModelScope.launch { settingsDataStore.updateStartDay(day) }
    fun updateAutoSave(enabled: Boolean) = viewModelScope.launch { settingsDataStore.updateAutoSave(enabled) }
    fun updateDarkMode(mode: String) = viewModelScope.launch { settingsDataStore.updateDarkMode(mode) }
    fun updateLanguage(lang: String) = viewModelScope.launch { settingsDataStore.updateLanguage(lang) }

    private fun loadReceipts() {
        viewModelScope.launch {
            getReceiptsUseCase().collect {
                _receipts.value = it
            }
        }
    }

    private fun loadMonthlyTotal() {
        viewModelScope.launch {
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            getMonthlyTotalUseCase(currentMonth).collect {
                _monthlyTotal.value = it
            }
        }
    }

    fun processOcrResult(textLines: List<String>, imagePath: String) {
        val parsed = ReceiptParser.parse(textLines, imagePath)
        // 사용자의 요청에 따라 촬영(또는 재촬영) 시 가맹점명과 날짜를 빈칸으로 초기화
        val clearedParsed = parsed.copy(storeName = "", date = "")
        
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
