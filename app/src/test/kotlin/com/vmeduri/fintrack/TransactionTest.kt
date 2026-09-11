package com.vmeduri.fintrack.data

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class TransactionTest {

    @Test
    fun testTransactionCreationWithDefaults() {
        val transaction = Transaction(
            amountCents = 10000,
            date = LocalDate.now(),
            category = Category.GENERAL_SPEND
        )

        assertEquals(0L, transaction.id)
        assertEquals(10000, transaction.amountCents)
        assertEquals(Category.GENERAL_SPEND, transaction.category)
        assertTrue(transaction.userId.isEmpty())
        assertNull(transaction.cloudId)
        assertEquals(Transaction.SyncStatus.PENDING, transaction.syncStatus)
    }

    @Test
    fun testTransactionWithCloudFields() {
        val transaction = Transaction(
            id = 1,
            amountCents = 5000,
            date = LocalDate.now(),
            category = Category.HEALTHY_FOOD,
            userId = "user123",
            cloudId = "cloud-id-456",
            syncStatus = Transaction.SyncStatus.SYNCED
        )

        assertEquals(1L, transaction.id)
        assertEquals("user123", transaction.userId)
        assertEquals("cloud-id-456", transaction.cloudId)
        assertEquals(Transaction.SyncStatus.SYNCED, transaction.syncStatus)
    }

    @Test
    fun testTransactionWithPendingStatus() {
        val transaction = Transaction(
            amountCents = 15000,
            date = LocalDate.now(),
            category = Category.HEALTHY_CARE,
            syncStatus = Transaction.SyncStatus.PENDING
        )

        assertEquals(Transaction.SyncStatus.PENDING, transaction.syncStatus)
    }

    @Test
    fun testTransactionWithErrorStatus() {
        val transaction = Transaction(
            amountCents = 20000,
            date = LocalDate.now(),
            category = Category.GENERAL_SPEND,
            syncStatus = Transaction.SyncStatus.ERROR
        )

        assertEquals(Transaction.SyncStatus.ERROR, transaction.syncStatus)
    }

    @Test
    fun testTransactionCopyWithUpdatedSyncStatus() {
        val original = Transaction(
            id = 1,
            amountCents = 10000,
            date = LocalDate.now(),
            category = Category.GENERAL_SPEND,
            syncStatus = Transaction.SyncStatus.PENDING
        )

        val updated = original.copy(
            syncStatus = Transaction.SyncStatus.SYNCED,
            cloudId = "new-cloud-id"
        )

        assertEquals(Transaction.SyncStatus.PENDING, original.syncStatus)
        assertNull(original.cloudId)
        assertEquals(Transaction.SyncStatus.SYNCED, updated.syncStatus)
        assertEquals("new-cloud-id", updated.cloudId)
    }
}
