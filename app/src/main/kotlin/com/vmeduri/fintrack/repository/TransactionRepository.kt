package com.vmeduri.fintrack.repository

import com.vmeduri.fintrack.data.AppDatabase
import com.vmeduri.fintrack.data.Category
import com.vmeduri.fintrack.data.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class TransactionRepository(
    private val database: AppDatabase,
    private val firestoreRepository: FirestoreRepository?
) {
    private val localDao = database.transactionDao()
    
    suspend fun insertOrUpdate(transaction: Transaction): Long {
        val localId = if (transaction.id == 0L) {
            localDao.insert(transaction)
        } else {
            localDao.update(transaction)
            transaction.id
        }
        
        firestoreRepository?.let { firestore ->
            val cloudId = firestore.saveTransaction(transaction.copy(id = localId))
            if (cloudId != null && transaction.cloudId != cloudId) {
                localDao.update(transaction.copy(
                    id = localId,
                    cloudId = cloudId,
                    syncStatus = Transaction.SyncStatus.SYNCED
                ))
            }
        }
        
        return localId
    }
    
    suspend fun delete(transaction: Transaction) {
        localDao.delete(transaction)
        transaction.cloudId?.let { cloudId ->
            firestoreRepository?.deleteTransaction(cloudId)
        }
    }
    
    suspend fun getById(id: Long): Transaction? {
        return localDao.getById(id)
    }
    
    fun observeAll(): Flow<List<Transaction>> {
        return if (firestoreRepository != null) {
            combine(
                localDao.observeAll(),
                firestoreRepository.observeAllTransactions()
            ) { local, cloud ->
                (local + cloud).distinctBy { it.cloudId ?: it.id }.sortedWith(
                    compareBy<Transaction> { it.date }.reversed()
                        .thenBy { it.createdAt }.reversed()
                )
            }
        } else {
            localDao.observeAll()
        }
    }
    
    fun observeTotalBetween(start: LocalDate, end: LocalDate): Flow<Long> {
        return if (firestoreRepository != null) {
            combine(
                localDao.observeTotalBetween(start, end),
                firestoreRepository.observeTotalBetween(start, end)
            ) { local, cloud ->
                maxOf(local, cloud)
            }
        } else {
            localDao.observeTotalBetween(start, end)
        }
    }
    
    fun observeTotalBetweenForCategory(
        start: LocalDate,
        end: LocalDate,
        category: Category
    ): Flow<Long> {
        return if (firestoreRepository != null) {
            combine(
                localDao.observeTotalBetweenForCategory(start, end, category),
                firestoreRepository.observeTotalBetweenForCategory(start, end, category)
            ) { local, cloud ->
                maxOf(local, cloud)
            }
        } else {
            localDao.observeTotalBetweenForCategory(start, end, category)
        }
    }
    
    suspend fun totalBetween(start: LocalDate, end: LocalDate): Long {
        return localDao.totalBetween(start, end)
    }
    
    suspend fun syncPendingTransactions() {
        firestoreRepository?.let { firestore ->
            val pending = localDao.observeAll().collect { transactions ->
                val mapping = firestore.syncPendingTransactions(transactions)
                mapping.forEach { (localId, cloudId) ->
                    val transaction = localDao.getById(localId) ?: return@forEach
                    localDao.update(transaction.copy(
                        cloudId = cloudId,
                        syncStatus = Transaction.SyncStatus.SYNCED
                    ))
                }
            }
        }
    }
}
