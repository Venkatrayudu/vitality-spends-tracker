package com.vmeduri.fintrack.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE date BETWEEN :start AND :end")
    fun observeTotalBetween(start: LocalDate, end: LocalDate): Flow<Long>

    @Query(
        "SELECT COALESCE(SUM(amountCents), 0) FROM transactions " +
            "WHERE date BETWEEN :start AND :end AND category = :category"
    )
    fun observeTotalBetweenForCategory(start: LocalDate, end: LocalDate, category: Category): Flow<Long>

    /**
     * One-shot (non-Flow) version of the same total, used from places that aren't
     * observing continuously: the background reminder Worker and the home screen widget.
     */
    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE date BETWEEN :start AND :end")
    suspend fun totalBetween(start: LocalDate, end: LocalDate): Long
}
