package com.vmeduri.fintrack.ui.addedit

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vmeduri.fintrack.data.AppDatabase
import com.vmeduri.fintrack.data.Category
import com.vmeduri.fintrack.data.Transaction
import com.vmeduri.fintrack.util.CurrencyUtils
import com.vmeduri.fintrack.widget.WidgetUpdater
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddEditViewModel(
    application: Application,
    private val transactionId: Long?
) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).transactionDao()

    var amountText by mutableStateOf("")
        private set
    var selectedCategory by mutableStateOf(Category.OTHER)
        private set
    var date by mutableStateOf(LocalDate.now())
        private set
    var note by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(transactionId != null)
        private set
    var saveError by mutableStateOf<String?>(null)
        private set

    init {
        val id = transactionId
        if (id != null) {
            viewModelScope.launch {
                val existing = dao.getById(id)
                if (existing != null) {
                    amountText = CurrencyUtils.centsToPlainString(existing.amountCents)
                    selectedCategory = existing.category
                    date = existing.date
                    note = existing.note.orEmpty()
                }
                isLoading = false
            }
        }
    }

    fun onAmountChange(value: String) {
        amountText = value
    }

    fun onCategoryChange(value: Category) {
        selectedCategory = value
    }

    fun onDateChange(value: LocalDate) {
        date = value
    }

    fun onNoteChange(value: String) {
        note = value
    }

    fun save(onDone: () -> Unit) {
        val cents = CurrencyUtils.parseRandsToCents(amountText)
        if (cents == null || cents <= 0) {
            saveError = "Enter a valid amount greater than zero."
            return
        }
        saveError = null

        viewModelScope.launch {
            val id = transactionId
            if (id == null) {
                dao.insert(
                    Transaction(
                        amountCents = cents,
                        date = date,
                        category = selectedCategory,
                        note = note.ifBlank { null }
                    )
                )
            } else {
                dao.update(
                    Transaction(
                        id = id,
                        amountCents = cents,
                        date = date,
                        category = selectedCategory,
                        note = note.ifBlank { null }
                    )
                )
            }
            WidgetUpdater.update(getApplication())
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = transactionId ?: return
        viewModelScope.launch {
            val existing = dao.getById(id) ?: return@launch
            dao.delete(existing)
            WidgetUpdater.update(getApplication())
            onDone()
        }
    }
}

class AddEditViewModelFactory(
    private val application: Application,
    private val transactionId: Long?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddEditViewModel(application, transactionId) as T
    }
}
