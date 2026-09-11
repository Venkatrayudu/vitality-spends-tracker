package com.vmeduri.fintrack.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vmeduri.fintrack.data.Category
import com.vmeduri.fintrack.data.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class FirestoreRepository(private val userId: String) {
    private val db = FirebaseFirestore.getInstance()
    private val transactionsCollection = db.collection("users").document(userId).collection("transactions")
    
    suspend fun saveTransaction(transaction: Transaction): String? {
        return try {
            val data = transaction.toFirestoreMap()
            val docRef = if (transaction.cloudId.isNullOrEmpty()) {
                transactionsCollection.add(data).await()
            } else {
                transactionsCollection.document(transaction.cloudId!!).set(data).await()
                transactionsCollection.document(transaction.cloudId!!)
            }
            docRef.id
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun deleteTransaction(cloudId: String): Boolean {
        return try {
            transactionsCollection.document(cloudId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getTransaction(cloudId: String): Transaction? {
        return try {
            val snapshot = transactionsCollection.document(cloudId).get().await()
            snapshot.toTransaction(userId)
        } catch (e: Exception) {
            null
        }
    }
    
    fun observeAllTransactions(): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionsCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toTransaction(userId)
                } ?: emptyList()
                
                trySend(transactions)
            }
        
        awaitClose { listener.remove() }
    }
    
    fun observeTotalBetween(start: LocalDate, end: LocalDate): Flow<Long> = flow {
        try {
            val startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val endDate = Date.from(end.plus(1, java.time.temporal.ChronoUnit.DAYS)
                .atStartOfDay(ZoneId.systemDefault()).toInstant())
            
            val snapshot = transactionsCollection
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .get()
                .await()
            
            val total = snapshot.documents.mapNotNull { doc ->
                (doc.get("amountCents") as? Number)?.toLong() ?: 0L
            }.sum()
            
            emit(total)
        } catch (e: Exception) {
            emit(0L)
        }
    }
    
    fun observeTotalBetweenForCategory(
        start: LocalDate,
        end: LocalDate,
        category: Category
    ): Flow<Long> = flow {
        try {
            val startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val endDate = Date.from(end.plus(1, java.time.temporal.ChronoUnit.DAYS)
                .atStartOfDay(ZoneId.systemDefault()).toInstant())
            
            val snapshot = transactionsCollection
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .whereEqualTo("category", category.name)
                .get()
                .await()
            
            val total = snapshot.documents.mapNotNull { doc ->
                (doc.get("amountCents") as? Number)?.toLong() ?: 0L
            }.sum()
            
            emit(total)
        } catch (e: Exception) {
            emit(0L)
        }
    }
    
    suspend fun syncPendingTransactions(transactions: List<Transaction>): Map<Long, String> {
        val mapping = mutableMapOf<Long, String>()
        
        for (transaction in transactions.filter { it.syncStatus == Transaction.SyncStatus.PENDING }) {
            val cloudId = saveTransaction(transaction)
            if (cloudId != null) {
                mapping[transaction.id] = cloudId
            }
        }
        
        return mapping
    }
    
    private fun Transaction.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "amountCents" to amountCents,
            "date" to Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            "category" to category.name,
            "note" to (note ?: ""),
            "createdAt" to createdAt,
            "syncTimestamp" to System.currentTimeMillis()
        )
    }
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toTransaction(userId: String): Transaction? {
        return try {
            val dateObj = this.get("date") as? Date ?: return null
            val localDate = dateObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            
            Transaction(
                id = 0,
                amountCents = (this.get("amountCents") as? Number)?.toLong() ?: 0,
                date = localDate,
                category = Category.valueOf(this.get("category") as? String ?: "GENERAL_SPEND"),
                note = this.get("note") as? String,
                createdAt = (this.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                userId = userId,
                cloudId = this.id,
                syncStatus = Transaction.SyncStatus.SYNCED,
                syncTimestamp = (this.get("syncTimestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
