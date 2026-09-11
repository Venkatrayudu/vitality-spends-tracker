package com.vmeduri.fintrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * One manually-entered card spend.
 *
 * Amounts are stored in cents (Long) rather than as a Double/Float so that money math
 * (summing, comparing against goals) is always exact — floating point rounding errors
 * are a classic source of "why is my total 1 cent off" bugs in finance apps.
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val date: LocalDate,
    val category: Category,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    
    // Cloud sync fields
    val userId: String = "",
    val cloudId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val syncTimestamp: Long = 0L
) {
    enum class SyncStatus {
        PENDING,
        SYNCED,
        ERROR
    }
}
